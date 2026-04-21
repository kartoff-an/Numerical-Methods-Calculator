# Numerical Methods Calculator

A JavaFX-based desktop application for solving common numerical methods problems with configurable precision and rounding options.

![Java Version](https://img.shields.io/badge/Java-17%2B-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-17%2B-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## Features

### Implemented Methods

| Method | Description |
|--------|-------------|
| **Newton-Raphson** | Root-finding algorithm using function derivatives |
| **Gauss-Jacobi** | Iterative method for solving systems of linear equations |
| **Newton's Interpolation** | Polynomial interpolation using divided differences |
| **Simpson's 1/3 Rule** | Numerical integration with quadratic polynomial approximation |
| **Central Divided Difference** | Numerical differentiation with Richardson extrapolation for improved accuracy |

## Configurable Settings

- **Decimal Places**: Set precision from 0 to 15 decimal places
- **Rounding Modes**:
  - **Round Every Iteration**: Apply rounding at each calculation step
  - **No Rounding**: Keep full precision during calculations

## Screenshots
### Main Interface
![Numerical Methods Calculator](APP_SCREENSHOT.png)

## Method Details
### 1. Newton-Raphson Method
**Purpose**: Find roots of nonlinear equations

**Inputs**:
- Function f(x)
- Initial guess x₀
- Tolerance
- Maximum iterations

**Outputs**:
- Root value
- Convergence status
- Iteration table showing x, f(x), f'(x), and error per iteration

### 2. Gauss-Jacobi Method
**Purpose**: Solve systems of linear equations iteratively

**Inputs**:
- Coefficient matrix A
- Constant vector b
- Initial guess
- Tolerance
- Maximum iterations

**Outputs**:
- Solution vector
- Convergence status
- Iteration history

### 3. Newton's Interpolation
**Purpose**: Construct interpolating polynomial from data points

**Inputs**:
- x-values array
- y-values array
- Interpolation points

**Outputs**:
- Divided differences table
- Interpolating polynomial (simplified form)
- Interpolated values at specified points

### 4. Simpson's 1/3 Rule
**Purpose**: Numerical integration of definite integrals

**Inputs**:
- Function f(x)
- Lower bound a
- Upper bound b
- Number of segments n (must be even, 2-8 recommended)

**Outputs**:
- Integral approximation
- Segment count
- Detailed calculation table showing points, function values, and coefficients

### 4. Central Divided Difference with Richardson Extrapolation
**Purpose**: Numerical differentiation with enhanced accuracy

**Inputs**:
- Function f(x)
- Point of differentiation x
- Initial step size h

**Outputs**:
- First to fourth derivative approximation
- Richardson extrapolation table
- Error estimates

## Installation
### Prerequisites
- Java 17 or higher
- JavaFX 17+
- Maven 3.6+ (for building)

### Building from Source

1. Clone the repository:
```bash
git clone https://github.com/kartoff-an/Numerical-Methods-Calculator.git
cd numerical-methods-calculator
```

2. Build with Maven
```bash
mvn clean package
```

3. Run the application
```bash
mvn javafx:run
```

4. Running the JAR
```
java -jar target/[NAME_OF_THE_APPLICATION]-1.0-SNAPSHOT.jar
```

## Usage Guide
### Basic Workflow
1. Select a method from the left sidebar
2. Configure settings in the right panel:
   - Set decimal places (0-15)
   - Choose rounding mode
3. Enter inputs for the selected method
4. Click "Solve" to compute results
5. View results in the workspace area with detailed tables

## Tips for Best Results
- Newton-Raphson - Choose initial guess close to the expected root
- Gauss-Jacobi - Ensure matrix is diagonally dominant for convergence
- Newton Interpolation - Use equally spaced points for best results
- Simpson's Rule - Use more segments for higher accuracy (must be even)
- Central Difference - Smaller step sizes aren't always better due to roundoff error

## Author
**Rean Glenn Roquero**

## Acknowledgements
- Numerical methods algorithms based on standard mathematical texts
- exp4j library for safe expression evaluation
- JavaFX community for UI framework support


**Note:** This calculator is built as a requirement for the course **CPE 221: Numerical Methods**.
