package org.example;

import java.util.Random;

public class Resources {
        public static int[] fillVectorNum(int size, int num) {
            int[] vector = new int[size];

            for (int i = 0; i < size; i++) {
                vector[i] = num;
            }

            return vector;
        }

    public static int[] fillVectorRandom(int size) {
        int[] vector = new int[size];
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            vector[i] = random.nextInt(10) + 1; // Генеруємо випадкове число від 1 до 10
        }

        return vector;
    }

        public static int[][] fillMatrixNum(int size, int num) {
            int[][] matrix = new int[size][size];

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    matrix[i][j] = num;
                }
            }
            return matrix;
        }

    public static int[][] fillMatrixRandom(int size) {
        int[][] matrix = new int[size][size];
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextInt(10) + 1; // Генеруємо випадкове число від 1 до 10
            }
        }
        return matrix;
    }

    public static int[] matrixTorow(int[][] matrix, int n) {
        int[] flatMatrix = new int[n * n];
        for (int j = 0; j < n; j++) { // Ітерація по стовпцях
            for (int i = 0; i < n; i++) { // Ітерація по рядках
                flatMatrix[j * n + i] = matrix[i][j];
            }
        }
        return flatMatrix;
    }

    public static int[] matrixToColumnNotSq(int[][] matrix, int rows, int cols, int skipCount) {
        // Розмір результату - це rows * cols
        int[] flatMatrix = new int[rows * cols];

        // Ініціалізація масиву нулями
        for (int i = 0; i < flatMatrix.length; i++) {
            flatMatrix[i] = 0;
        }

        int index = skipCount;  // Початковий індекс в результативному масиві для запису значень

        // Ітерація по стовпцях
        for (int j = 0; j < cols; j++) {
            // Ітерація по рядках
            for (int i = 0; i < rows; i++) {
                // Перевіряємо, чи є значення в поточному елементі матриці
                if (i < matrix.length && j < matrix[i].length) {
                    // Переміщаємо значення в результативний масив, пропускаючи кількість елементів
                    flatMatrix[index] = matrix[i][j];
                    index++;  // Переміщаємо індекс для наступного елементу
                } else {
                    // Якщо елемент відсутній, пропускаємо значення (залишаємо 0)
                    index++;
                }
            }
        }
        return flatMatrix;
    }



    public static int[][] rowToMatrix(int[] flatMatrix, int startIndex, int rows, int cols) {

        int[][] matrix = new int[rows][cols]; // Ініціалізуємо матрицю розміру rows x cols

        for (int i = 0; i < rows; i++) {  // Ітерація по рядках
            for (int j = 0; j < cols; j++) {  // Ітерація по стовпцях
                matrix[i][j] = flatMatrix[startIndex + i + j * rows];  // Перетворюємо елементи з вектора в матрицю
            }
        }
        return matrix;
    }




    public static void printVector(int[] vector) {
        for (int i = 0; i < 8; i++) {
            System.out.print(vector[i] + " ");
        }
        System.out.println("...");
    }

    public static void printMatrix(int[][] matrix) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println("...");
        }
    }

    public static void partialMulMatrixMatrix(
                int[][] matrix1, int[][] matrix2, int[][] result, int start, int end) {
            for (int j = start; j < end; j++) {
                for (int i = 0; i < matrix1.length; i++) {
                    result[i][j] = 0;
                    for (int k = 0; k < matrix1[0].length; k++) {
                        result[i][j] += matrix1[i][k] * matrix2[k][j];
                    }
                }
            }
        }
    public static void computePartialResult(int[][] MM, int[][] MR, int[][] MC, int[][] result, int start, int end) {
        int[][] intermediateResult = new int[result.length][result.length];

        // Множимо MR на MC, результат зберігається в intermediateResult
        partialMulMatrixMatrix(MR, MC, intermediateResult, start, end);


        // Тепер множимо MM на intermediateResult, результат в result
        partialMulMatrixMatrix(MM, intermediateResult, result, start, end);
    }

    public static void multiplyScalarOnMatrixColumns(int[][] matrix, int scalar, int start, int end) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = start; j < end; j++) {
                matrix[i][j] *= scalar;
            }
        }
    }

    public static void subtractMatricesByColumns(int[][] A, int[][] B, int startCol, int endCol) {
        for (int j = startCol; j < endCol; j++) {
            for (int i = 0; i < A.length; i++) {
                A[i][j] -= B[i][j];
            }
        }
    }

        public static int findMax_mi(int[] vector, int start, int end) {
            int max = Integer.MIN_VALUE;

            for (int i = start; i < end; i++) {
                if (vector[i] > max) {
                    max = vector[i];
                }
            }
            return max;
        }
    }
