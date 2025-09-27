# ========== Linked list & DB ==========
mutable struct Node
    row::Vector{String}
    next::Union{Nothing, Node}
end

mutable struct MemoryDB
    header::Vector{String}
    head::Union{Nothing, Node}
end

# ========== CSV parsing ==========
function parse_csv_line(line::String)
    out = String[]
    buf = IOBuffer()
    inq = false
    i = firstindex(line)
    while i <= lastindex(line)
        c = line[i]
        if inq
            if c == '"'
                if i < lastindex(line) && line[i+1] == '"'
                    print(buf, '"'); i += 1
                else
                    inq = false
                end
            else
                print(buf, c)
            end
        else
            if c == ','
                push!(out, String(take!(buf)))
            elseif c == '"'
                inq = true
            else
                print(buf, c)
            end
        end
        i += 1
    end
    push!(out, String(take!(buf)))
    return out
end

function read_csv(path::String)
    lines = readlines(path)
    isempty(lines) && error("Empty CSV: $path")
    header = parse_csv_line(lines[1])
    rows = Vector{Vector{String}}()
    for i in 2:length(lines)
        s = strip(lines[i])
        !isempty(s) && push!(rows, parse_csv_line(lines[i]))
    end
    return header, rows
end

# ========== Recursive build ==========
function build_list(rows::Vector{Vector{String}}, i::Int=1)
    i > length(rows) && return nothing
    Node(rows[i], build_list(rows, i+1))
end
MemoryDB(header::Vector{String}, rows::Vector{Vector{String}}) = MemoryDB(header, build_list(rows, 1))

# ========== Compare & sorts ==========
function tryparsefloat(s::String)
    try parse(Float64, strip(s)) catch; nothing end
end

function cmp_nodes(a::Node, b::Node, col::Int, asc::Bool)
    sa, sb = a.row[col], b.row[col]
    da, db = tryparsefloat(sa), tryparsefloat(sb)
    c = (da !== nothing && db !== nothing) ? cmp(da::Float64, db::Float64) : cmp(lowercase(sa), lowercase(sb))
    return asc ? c : -c
end

function bubble_sort!(db::MemoryDB, col::Int, asc::Bool=true)
    head = db.head
    head === nothing && return
    while true
        swapped = false
        cur = head
        while cur !== nothing && cur.next !== nothing
            if cmp_nodes(cur, cur.next, col, asc) > 0
                cur.row, cur.next.row = cur.next.row, cur.row
                swapped = true
            end
            cur = cur.next
        end
        swapped || break
    end
end

function insertion_sort!(db::MemoryDB, col::Int, asc::Bool=true)
    dummy = Node(String[], nothing)
    cur = db.head
    while cur !== nothing
        nxt = cur.next
        prev = dummy
        while prev.next !== nothing && cmp_nodes(prev.next, cur, col, asc) <= 0
            prev = prev.next
        end
        cur.next = prev.next
        prev.next = cur
        cur = nxt
    end
    db.head = dummy.next
end

# ========== Recursive export ==========
function write_csv_recursive(io::IO, header::Vector{String}, n::Union{Nothing,Node})
    println(io, join(header, ","))
    _write_rows(io, n)
end
function _write_rows(io::IO, n::Union{Nothing,Node})
    n === nothing && return
    escaped = map(n.row) do s
        if occursin(",", s) || occursin("\"", s)
            "\"$(replace(s, "\"" => "\"\""))\""
        else s
        end
    end
    println(io, join(escaped, ","))
    _write_rows(io, n.next)
end

# ========== Utilities ==========
function resolve_col(header::Vector{String}, col::String)
    idx_try = try parse(Int, col) + 1 catch; nothing end  # allow 0-based index input
    if idx_try !== nothing
        return idx_try
    end
    for (i,h) in enumerate(header)
        if lowercase(h) == lowercase(col)
            return i
        end
    end
    error("Column not found: $col")
end

import Dates: format, now

# NEW: put exports under julia/out/outputs to match Java's java/out/outputs
const JULIA_OUT_DIR = joinpath(@__DIR__, "out")

function export_path(keys::Vector{Tuple{Symbol,String}})
    base = joinpath(JULIA_OUT_DIR, "sorted")
    for (a,c) in keys
        base *= "_" * (a==:b ? "b" : "i") * "-" * replace(c, r"[^A-Za-z0-9_-]" => "")
    end
    base *= "_" * format(now(), "yyyymmdd_HHMMss") * ".csv"
    return base
end

# ========== Interactive CLI ==========
function main()
    # ensure outputs dir (julia/out/outputs)
    try; mkpath(JULIA_OUT_DIR); catch; end

    print("Enter CSV path [data/student-data.csv]: ")
    input = String(strip(readline(stdin)))
    input == "" && (input = "data/student-data.csv")

    header, rows = read_csv(input)
    db = MemoryDB(header, rows)
    println("Loaded rows: $(length(rows))")

    # store multi-level keys, first chosen is primary
    keys = Tuple{Symbol,String}[]   # (:b or :i, col string)

    while true
        print("Choose sort algorithm [b=bubble, i=insertion]: ")
        algo = lowercase(String(strip(readline(stdin))))
        while !(algo in ("b","i"))
            print("Please enter 'b' or 'i': ")
            algo = lowercase(String(strip(readline(stdin))))
        end
        a_sym = algo == "b" ? :b : :i

        print("Sort by which column (name or 0-based index): ")
        col = String(strip(readline(stdin)))
        if col == ""
            println("Column cannot be empty. Try again."); continue
        end
        push!(keys, (a_sym, col))

        # Apply stable sorts in REVERSE order so first chosen remains highest priority
        for k in reverse(keys)
            colidx = resolve_col(db.header, k[2])
            if k[1] == :b
                bubble_sort!(db, colidx, true)
            else
                insertion_sort!(db, colidx, true)
            end
        end

        println(
            "Sorted by keys (primary→secondary): " *
            join(map(k -> ((k[1] == :b) ? "bubble" : "insertion") * "(" * k[2] * ")", keys), " > ")
        )

        print("Options: [e=export+quit, ec=export+continue, c=continue, q=quit] : ")
        opt = lowercase(String(strip(readline(stdin))))
        if opt == "e" || opt == "ec"
            out = export_path(keys)
            open(out, "w") do io
                write_csv_recursive(io, db.header, db.head)
            end
            println("Exported: $out")
            if opt == "e"
                println("Bye!"); break
            end
            # else continue
        elseif opt == "q"
            println("Bye!"); break
        else
            # c or anything else -> continue
        end
    end
end

main()
