# Individual Homework-3: Sorting Memory Database

## 📌 Overview
This project implements a toy memory database using a linked list with support for two sorting algorithms:
- **Bubble Sort**
- **Insertion Sort**

The assignment requirements included:
1. Recursively loading `student-data.csv` into memory.  
2. Sorting the data using bubble sort and insertion sort.  
3. Recursively exporting the sorted data to CSV files.  
4. Comparing CPU and disk usage of both algorithms on Ubuntu using Linux performance tools.  

The solution is implemented in **Julia** and **Java**, and was tested in both Windows (for quick checks) and Ubuntu 22.04 (for final execution and profiling).

---

## 📂 Project Structure
```
algorithms-hw3/
├─ data/        
│  └─ student-data.csv        # dataset
├─ java/        
│  ├─ Node.java
│  ├─ MemoryDB.java
│  ├─ CsvUtils.java
│  └─ Main.java
│  └─ out/                    # compiled .class files (ignored in git)
├─ julia/       
│  └─ main.jl
├─ perf/                      # performance logs (optional)
├─ report/
│  └─ Final_Individual_Homework_3_Vaishak_Balachandra.pdf
├─ README.md
└─ .gitignore
```

---

## ▶️ Running the Code

### Java
```bash
cd java
mkdir -p out
javac -d out *.java
java -cp out Main
```

### Julia
```bash
julia julia/main.jl
```

Both programs are interactive:
1. Enter dataset path (e.g., `data/student-data.csv`)  
2. Choose sorting algorithm:  
   - `b` → Bubble Sort  
   - `i` → Insertion Sort  
3. Enter column name or index (e.g., `Age` or `0`)  
4. Choose operation:  
   - `e` → Export most recent + quit  
   - `ec` → Export most recent + continue  
   - `c` → Continue with another column  
   - `q` → Quit without exporting  

Sorted CSV files are saved in:
- `java/out/`
- `julia/out/`

---

## 📊 Performance Analysis
Performance profiling was conducted on Ubuntu using:
- `/usr/bin/time -v` for CPU & memory usage  
- `iostat` for disk activity  

Results showed that **insertion sort consistently outperformed bubble sort**, while disk usage remained negligible since sorting operations were memory-bound.

---

## 📑 Report
The full report is available here:
```
report/Final_Individual_Homework_3_Vaishak_Balachandra.pdf
```

---

## 🌐 OnlineGDB
The Java implementation can also be tested via OnlineGDB:  
👉 [OnlineGDB Link](https://www.onlinegdb.com/) *(insert your shared link here)*

---

## ⚖️ License
This repository is for **academic purposes only**.

