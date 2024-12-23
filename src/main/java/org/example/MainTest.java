package org.example;
import mpi.MPI;

import java.util.Scanner;

import static java.lang.Thread.sleep;

public class MainTest {
    public static void main(String[] args) throws InterruptedException {
        sleep(30000);
        MPI.Init(args);
        long startTime = System.currentTimeMillis();
        int rank = MPI.COMM_WORLD.Rank();
        int[] N = new int[1];
        if (rank == 0) {
            N[0] = 1000;
        }
        MPI.COMM_WORLD.Bcast(N, 0, 1, MPI.INT, 0);
        int P = 8;
        int H = N[0] / P;

        int[] R;

        int[][] MR;
        int[][] MM;
        int[][] MC;
        int[][] MS;

        int[][] MZ = new int[N[0]][N[0]];

        System.out.println("Thread T" + (rank + 1) + " started!");

        switch (rank) {
            case 0: {
                // Введення MR, MM, R, MC, MS
                R = Resources.fillVectorNum(N[0], 1);
                MM = Resources.fillMatrixNum(N[0], 1);
                MR = Resources.fillMatrixNum(N[0], 1);
                MC = Resources.fillMatrixNum(N[0], 1);
                MS = Resources.fillMatrixNum(N[0], 1);

                int[] sendMM = Resources.matrixTorow(MM, N[0]);
                int[] sendMR = Resources.matrixTorow(MR, N[0]);
                int[] sendMC = Resources.matrixTorow(MC, N[0]);

                int[] sendMS = Resources.matrixTorow(MS, N[0]);

                // Передати R[h..2h], MC[h..2h], MS[h..2h], MR, MM до T2
                MPI.COMM_WORLD.Send(R, H, H, MPI.INT, 1, 0);

                MPI.COMM_WORLD.Send(sendMC, H*N[0], H*N[0], MPI.INT, 1, 0);
                MPI.COMM_WORLD.Send(sendMS, H*N[0], H*N[0], MPI.INT, 1, 0);

                MPI.COMM_WORLD.Send(sendMR, 0, N[0]*N[0], MPI.INT, 1, 0);
                MPI.COMM_WORLD.Send(sendMM, 0, N[0]*N[0], MPI.INT, 1, 0);


                // Передати R[2h..3h][3h..4h], MC[2h..3h][3h..4h], MS[2h..3h][3h..4h], MR, MM до T3
                MPI.COMM_WORLD.Send(R, 2*H, 2*H, MPI.INT, 2, 0);

                MPI.COMM_WORLD.Send(sendMC, 2*H*N[0], 2*H*N[0], MPI.INT, 2, 0);
                MPI.COMM_WORLD.Send(sendMS, 2*H*N[0], 2*H*N[0], MPI.INT, 2, 0);

                MPI.COMM_WORLD.Send(sendMR, 0, N[0]*N[0], MPI.INT, 2, 0);
                MPI.COMM_WORLD.Send(sendMM, 0, N[0]*N[0], MPI.INT, 2, 0);


                // Передати R[4h..5h][5h..6h][6h..7h][7h..8h], MC[4h..5h][5h..6h][6h..7h][7h..8h], MS[4h..5h][5h..6h][6h..7h][7h..8h], MR, MM до T5
                MPI.COMM_WORLD.Send(R, 4*H, 4*H, MPI.INT, 4, 0);

                MPI.COMM_WORLD.Send(sendMC, 4*H*N[0], 4*H*N[0], MPI.INT, 4, 0);
                MPI.COMM_WORLD.Send(sendMS, 4*H*N[0], 4*H*N[0], MPI.INT, 4, 0);

                MPI.COMM_WORLD.Send(sendMR, 0, N[0]*N[0], MPI.INT, 4, 0);
                MPI.COMM_WORLD.Send(sendMM, 0, N[0]*N[0], MPI.INT, 4, 0);

                //Обчислення 1: p1 = max(Rh)
                int [] p1 = {Resources.findMax_mi(R, 0, H)};

                int [] p2 = new int [1];
                int [] p5 = new int [1];

                //Прийняти від T2 p2
                MPI.COMM_WORLD.Recv(p2, 0, 1, MPI.INT, 1, 0);

                //Прийняти від T5 p5
                MPI.COMM_WORLD.Recv(p5, 0, 1, MPI.INT, 4, 0);

                int [] p = {Math.max(p1[0], Math.max(p2[0], p5[0]))};

                System.out.println("[T1]: I got p: " + p[0]);

                //Передати p до Т2
                MPI.COMM_WORLD.Send(p, 0, 1, MPI.INT, 1, 0);
                //Передати p до Т3
                MPI.COMM_WORLD.Send(p, 0, 1, MPI.INT, 2, 0);
                //Передати p до Т5
                MPI.COMM_WORLD.Send(p, 0, 1, MPI.INT, 4, 0);


                //Обчислення 3: MZh = MM*(MR*MCh) - p*MSh
                Resources.multiplyScalarOnMatrixColumns(MS, p[0], 0, H);
                Resources.computePartialResult(MM, MR, MC, MZ, 0, H);
                Resources.subtractMatricesByColumns(MZ, MS, 0, H);

                int [] finalMZvector = Resources.matrixToColumnNotSq(MZ, N[0], N[0], 0);

                //Прийняти MZ[h..2h][2h..3h][3h..4h] від Т2
                MPI.COMM_WORLD.Recv(finalMZvector, H*N[0], 3*H*N[0], MPI.INT, 1, 0);

                //Прийняти MZ[4h..5h][5h..6h][6h..7h][7h..8h] від Т5
                MPI.COMM_WORLD.Recv(finalMZvector, 4*H*N[0], 4*H*N[0], MPI.INT, 4, 0);
                System.out.print("[T1]: vectorMZ (with [h..2h][2h..3h][3h..4h][4h..5h][5h..6h][6h..7h][7h..8h]) = ");
                Resources.printVector(finalMZvector);

                MZ = Resources.rowToMatrix(finalMZvector, 0, N[0], N[0]);
                System.out.println("Final result MZ = ");
                Resources.printMatrix(MZ);

                break;
            }
            case 1: {
                int[] receivedR_H_2H = new int[H];

                int[] receivedMC_H_2H = new int[H*N[0]];
                int[] receivedMS_H_2H = new int[H*N[0]];

                int[] receivedMM = new int[N[0]*N[0]];
                int[] receivedMR = new int[N[0]*N[0]];

                // Прийняти від Т1 R[h..2h], MC[h..2h], MS[h..2h], MR, MM
                MPI.COMM_WORLD.Recv(receivedR_H_2H, 0, H, MPI.INT, 0, 0);
                MPI.COMM_WORLD.Recv(receivedMC_H_2H, 0, H*N[0], MPI.INT, 0, 0);
                MPI.COMM_WORLD.Recv(receivedMS_H_2H, 0, H*N[0], MPI.INT, 0, 0);

                MPI.COMM_WORLD.Recv(receivedMR, 0, N[0]*N[0], MPI.INT, 0, 0);
                MPI.COMM_WORLD.Recv(receivedMM, 0, N[0]*N[0], MPI.INT, 0, 0);

                int [][] MC_H_2H =  Resources.rowToMatrix(receivedMC_H_2H, 0, N[0], H);
                int [][] MS_H_2H =  Resources.rowToMatrix(receivedMS_H_2H, 0, N[0], H);
                int [][] MR_2 =  Resources.rowToMatrix(receivedMR, 0, N[0], N[0]);
                int [][] MM_2 =  Resources.rowToMatrix(receivedMM,0, N[0], N[0]);

                System.out.println("T2: I got everything!");

                //Обчислення 1: p2 = max(Rh)
                int [] p2 = {Resources.findMax_mi(receivedR_H_2H, 0, H)};
                int [] p4 = new int[1];

                //Прийняти від T4 p4
                MPI.COMM_WORLD.Recv(p4, 0, 1, MPI.INT, 3, 0);

                //Обчислення 2: p2 = max(p2, p4)
                p2[0] = Math.max(p2[0], p4[0]);

                //Передати до T1 p2
                MPI.COMM_WORLD.Send(p2, 0, 1, MPI.INT, 0, 0);


                //Прийняти від T1 p
                int [] p = new int[1];
                MPI.COMM_WORLD.Recv(p, 0, 1, MPI.INT, 0, 0);
                System.out.println("[T2]: I got p: " + p[0]);

                //Передати до Т4 p
                MPI.COMM_WORLD.Send(p, 0, 1, MPI.INT, 3, 0);

                //Передати до Т7 p
                MPI.COMM_WORLD.Send(p, 0, 1, MPI.INT, 6, 0);



                //Обчислення 3: MZh = MM*(MR*MCh) - p*MSh
                int [][] MZ_H_2H = new int[N[0]][H];

                Resources.multiplyScalarOnMatrixColumns(MS_H_2H, p[0], 0, H);
                Resources.computePartialResult(MM_2, MR_2, MC_H_2H, MZ_H_2H, 0, H);
                Resources.subtractMatricesByColumns(MZ_H_2H, MS_H_2H, 0, H);

                int [] sendMZ_H_4H = Resources.matrixToColumnNotSq(MZ_H_2H, N[0], 3*H, 0);

                //Прийняти MZ[2h..3h][3h..4h] від Т4
                MPI.COMM_WORLD.Recv(sendMZ_H_4H, H*N[0], 2*H*N[0], MPI.INT, 3, 0);

                //Передати до Т1 MZ[h..2h][2h..3h][3h..4h]
                MPI.COMM_WORLD.Send(sendMZ_H_4H, 0, 3*H*N[0], MPI.INT, 0, 0);

                break;
            }
            case 2: {
                //Прийняти від Т1 R[2h..3h][3h..4h], MC[2h..3h][3h..4h], MS[2h..3h][3h..4h], MR, MM
                int[] receivedR_2H_4H = new int[2*H];

                int[] receivedMC_2H_4H = new int[2*H*N[0]];
                int[] receivedMS_2H_4H = new int[2*H*N[0]];

                int[] receivedMM = new int[N[0]*N[0]];
                int[] receivedMR = new int[N[0]*N[0]];

                MPI.COMM_WORLD.Recv(receivedR_2H_4H, 0, 2*H, MPI.INT, 0, 0);
                MPI.COMM_WORLD.Recv(receivedMC_2H_4H, 0, 2*H*N[0], MPI.INT, 0, 0);
                MPI.COMM_WORLD.Recv(receivedMS_2H_4H, 0, 2*H*N[0], MPI.INT, 0, 0);

                MPI.COMM_WORLD.Recv(receivedMR, 0, N[0]*N[0], MPI.INT, 0, 0);
                MPI.COMM_WORLD.Recv(receivedMM, 0, N[0]*N[0], MPI.INT, 0, 0);

                int [][] MC_2H_3H =  Resources.rowToMatrix(receivedMC_2H_4H, 0, N[0], H);
                int [][] MS_2H_3H =  Resources.rowToMatrix(receivedMS_2H_4H, 0, N[0], H);
                int [][] MR_3 =  Resources.rowToMatrix(receivedMR, 0, N[0], N[0]);
                int [][] MM_3 =  Resources.rowToMatrix(receivedMM, 0, N[0], N[0]);

                System.out.println("T3: I got everything!");

                // Передати R[3h..4h], MC[3h..4h], MS[3h..4h], MR, MM до T4
                MPI.COMM_WORLD.Send(receivedR_2H_4H, H, H, MPI.INT, 3, 0);

                MPI.COMM_WORLD.Send(receivedMC_2H_4H, H*N[0], H*N[0], MPI.INT, 3, 0);
                MPI.COMM_WORLD.Send(receivedMS_2H_4H, H*N[0], H*N[0], MPI.INT, 3, 0);

                MPI.COMM_WORLD.Send(receivedMR, 0, N[0]*N[0], MPI.INT, 3, 0);
                MPI.COMM_WORLD.Send(receivedMM, 0, N[0]*N[0], MPI.INT, 3, 0);


                //Обчислення 1: p3 = max(Rh)
                int [] p3 = {Resources.findMax_mi(receivedR_2H_4H, 0, H)};

                //Передати до Т4 р3
                MPI.COMM_WORLD.Send(p3, 0, 1, MPI.INT, 3, 0);


                //Прийняти від T1 p
                int [] p = new int[1];
                MPI.COMM_WORLD.Recv(p, 0, 1, MPI.INT, 0, 0);
                System.out.println("[T3]: I got p: " + p[0]);

                //Передати до Т6 р
                MPI.COMM_WORLD.Send(p, 0, 1, MPI.INT, 5, 0);


                //Обчислення 3: MZh = MM*(MR*MCh) - p*MSh
                int [][] MZ_2H_3H = new int[N[0]][H];

                Resources.multiplyScalarOnMatrixColumns(MS_2H_3H, p[0], 0, H);
                Resources.computePartialResult(MM_3, MR_3, MC_2H_3H, MZ_2H_3H, 0, H);
                Resources.subtractMatricesByColumns(MZ_2H_3H, MS_2H_3H, 0, H);

                int [] sendMZ_2H_3H = Resources.matrixToColumnNotSq(MZ_2H_3H, N[0], H, 0);

                //Передати до Т4 MZ[2h..3h]
                MPI.COMM_WORLD.Send(sendMZ_2H_3H, 0, H*N[0], MPI.INT, 3, 0);

                break;

            }
            case 3: {
                //Прийняти від Т1 R[2h..3h][3h..4h], MC[2h..3h][3h..4h], MS[2h..3h][3h..4h], MR, MM
                int[] receivedR_3H_4H = new int[H];

                int[] receivedMC_3H_4H = new int[H*N[0]];
                int[] receivedMS_3H_4H = new int[H*N[0]];

                int[] receivedMM = new int[N[0]*N[0]];
                int[] receivedMR = new int[N[0]*N[0]];

                MPI.COMM_WORLD.Recv(receivedR_3H_4H, 0, H, MPI.INT, 2, 0);
                MPI.COMM_WORLD.Recv(receivedMC_3H_4H, 0, H*N[0], MPI.INT, 2, 0);
                MPI.COMM_WORLD.Recv(receivedMS_3H_4H, 0, H*N[0], MPI.INT, 2, 0);

                MPI.COMM_WORLD.Recv(receivedMR, 0, N[0]*N[0], MPI.INT, 2, 0);
                MPI.COMM_WORLD.Recv(receivedMM, 0, N[0]*N[0], MPI.INT, 2, 0);

                int [][] MC_3H_4H =  Resources.rowToMatrix(receivedMC_3H_4H, 0, N[0], H);
                int [][] MS_3H_4H =  Resources.rowToMatrix(receivedMS_3H_4H, 0, N[0], H);
                int [][] MR_4 =  Resources.rowToMatrix(receivedMR, 0, N[0], N[0]);
                int [][] MM_4 =  Resources.rowToMatrix(receivedMM, 0, N[0], N[0]);

                System.out.println("T4: I got everything!");

                //Обчислення 1: p4 = max(Rh)
                int [] p4 = {Resources.findMax_mi(receivedR_3H_4H, 0, H)};

                //Прийняти від T3 p3
                int [] p3 = new int [1];
                MPI.COMM_WORLD.Recv(p3, 0, 1, MPI.INT, 2, 0);

                //Обчислення 2: p4 = max(p4, p3)
                p4[0] = Math.max(p4[0], p3[0]);

                //Передати до Т2 р4
                MPI.COMM_WORLD.Send(p4, 0, 1, MPI.INT, 1, 0);

                //Прийняти від T2 p
                int [] p = new int[1];
                MPI.COMM_WORLD.Recv(p, 0, 1, MPI.INT, 1, 0);
                System.out.println("[T4]: I got p: " + p[0]);

                //Обчислення 3: MZh = MM*(MR*MCh) - p*MSh
                int [][] MZ_3H_4H = new int[N[0]][H];

                Resources.multiplyScalarOnMatrixColumns(MS_3H_4H, p[0], 0, H);
                Resources.computePartialResult(MM_4, MR_4, MC_3H_4H, MZ_3H_4H, 0, H);
                Resources.subtractMatricesByColumns(MZ_3H_4H, MS_3H_4H, 0, H);

                int [] sendMZ_2H_4H = Resources.matrixToColumnNotSq(MZ_3H_4H, N[0], 2*H, H*N[0]);

                // Прийняти від Т3 MZ[2h..3h]
                MPI.COMM_WORLD.Recv(sendMZ_2H_4H, 0, H*N[0], MPI.INT, 2, 0);

                // Передати до Т2 MZ[2h..3h][3h..4h]
                MPI.COMM_WORLD.Send(sendMZ_2H_4H, 0, 2*H*N[0], MPI.INT, 1, 0);

                break;
            }
            case 4: {
                //Прийняти від Т1 R[4h..5h][5h..6h][6h..7h][7h..8h], MC[4h..5h][5h..6h][6h..7h][7h..8h], MS[4h..5h][5h..6h][6h..7h][7h..8h], MR, MM
                int[] receivedR_4H_8H = new int[4*H];

                int[] receivedMC_4H_8H = new int[4*H*N[0]];
                int[] receivedMS_4H_8H = new int[4*H*N[0]];

                int[] receivedMM = new int[N[0]*N[0]];
                int[] receivedMR = new int[N[0]*N[0]];

                MPI.COMM_WORLD.Recv(receivedR_4H_8H, 0, 4*H, MPI.INT, 0, 0);

                MPI.COMM_WORLD.Recv(receivedMC_4H_8H, 0, 4*H*N[0], MPI.INT, 0, 0);
                MPI.COMM_WORLD.Recv(receivedMS_4H_8H, 0, 4*H*N[0], MPI.INT, 0, 0);

                MPI.COMM_WORLD.Recv(receivedMR, 0, N[0]*N[0], MPI.INT, 0, 0);
                MPI.COMM_WORLD.Recv(receivedMM, 0, N[0]*N[0], MPI.INT, 0, 0);

                int [][] MC_4H_5H =  Resources.rowToMatrix(receivedMC_4H_8H, 0, N[0], H);
                int [][] MS_4H_5H =  Resources.rowToMatrix(receivedMS_4H_8H, 0, N[0], H);
                int [][] MR_5 =  Resources.rowToMatrix(receivedMR, 0, N[0], N[0]);
                int [][] MM_5 =  Resources.rowToMatrix(receivedMM, 0, N[0], N[0]);

                System.out.println("T5: I got everything!");

                // Передати R[5h..6h], MC[5h..6h], MS[5h..6h], MR, MM до T6
                MPI.COMM_WORLD.Send(receivedR_4H_8H, H, H, MPI.INT, 5, 0);

                MPI.COMM_WORLD.Send(receivedMC_4H_8H, H*N[0], H*N[0], MPI.INT, 5, 0);
                MPI.COMM_WORLD.Send(receivedMS_4H_8H, H*N[0], H*N[0], MPI.INT, 5, 0);

                MPI.COMM_WORLD.Send(receivedMR, 0, N[0]*N[0], MPI.INT, 5, 0);
                MPI.COMM_WORLD.Send(receivedMM, 0, N[0]*N[0], MPI.INT, 5, 0);


                // Передати R[6h..7h][7h..8h], MC[6h..7h][7h..8h], MS[6h..7h][7h..8h], MR, MM до T7
                MPI.COMM_WORLD.Send(receivedR_4H_8H, 2*H, 2*H, MPI.INT, 6, 0);

                MPI.COMM_WORLD.Send(receivedMC_4H_8H, 2*H*N[0], 2*H*N[0], MPI.INT, 6, 0);
                MPI.COMM_WORLD.Send(receivedMS_4H_8H, 2*H*N[0], 2*H*N[0], MPI.INT, 6, 0);

                MPI.COMM_WORLD.Send(receivedMR, 0, N[0]*N[0], MPI.INT, 6, 0);
                MPI.COMM_WORLD.Send(receivedMM, 0, N[0]*N[0], MPI.INT, 6, 0);

                //Обчислення 1: p5 = max(Rh)
                int [] p5 = {Resources.findMax_mi(receivedR_4H_8H, 0, H)};

                //Прийняти від T6 p6
                int [] p6 = new int [1];
                MPI.COMM_WORLD.Recv(p6, 0, 1, MPI.INT, 5, 0);

                //Прийняти від T7 p7
                int [] p7 = new int [1];
                MPI.COMM_WORLD.Recv(p7, 0, 1, MPI.INT, 6, 0);

                p5[0] = Math.max(p5[0], Math.max(p6[0], p7[0]));

                //Передати до Т1 p5
                MPI.COMM_WORLD.Send(p5, 0, 1, MPI.INT, 0, 0);

                //Прийняти від T1 p
                int [] p = new int[1];
                MPI.COMM_WORLD.Recv(p, 0, 1, MPI.INT, 0, 0);
                System.out.println("[T5]: I got p: " + p[0]);

                int [][] MZ_4H_5H = new int[N[0]][H];
                //Обчислення 3: MZh = MM*(MR*MCh) - p*MSh
                Resources.multiplyScalarOnMatrixColumns(MS_4H_5H, p[0], 0, H);
                Resources.computePartialResult(MM_5, MR_5, MC_4H_5H, MZ_4H_5H, 0, H);
                Resources.subtractMatricesByColumns(MZ_4H_5H, MS_4H_5H, 0, H);

                int [] sendMZ_4H_8H = Resources.matrixToColumnNotSq(MZ_4H_5H, N[0], 4*H, 0);

                //Прийняти від Т6 MZ[5h..6h]
                MPI.COMM_WORLD.Recv(sendMZ_4H_8H, H*N[0], H*N[0], MPI.INT, 5, 0);

                //Прийняти від Т7 MZ[6h..7h][7h..8h]
                MPI.COMM_WORLD.Recv(sendMZ_4H_8H, 2*H*N[0], 2*H*N[0], MPI.INT, 6, 0);

                //Передати до Т1 MZ[4h..5h][5h..6h][6h..7h][7h..8h]
                MPI.COMM_WORLD.Send(sendMZ_4H_8H, 0, 4*H*N[0], MPI.INT, 0, 0);

                break;
            }
            case 5: {
                //Прийняти від Т5 R[5h..6h], MC[5h..6h], MS[5h..6h], MR, MM
                int[] receivedR_5H_6H = new int[H];

                int[] receivedMC_5H_6H = new int[H*N[0]];
                int[] receivedMS_5H_6H = new int[H*N[0]];

                int[] receivedMM = new int[N[0]*N[0]];
                int[] receivedMR = new int[N[0]*N[0]];

                MPI.COMM_WORLD.Recv(receivedR_5H_6H, 0, H, MPI.INT, 4, 0);

                MPI.COMM_WORLD.Recv(receivedMC_5H_6H, 0, H*N[0], MPI.INT, 4, 0);
                MPI.COMM_WORLD.Recv(receivedMS_5H_6H, 0, H*N[0], MPI.INT, 4, 0);

                MPI.COMM_WORLD.Recv(receivedMR, 0, N[0]*N[0], MPI.INT, 4, 0);
                MPI.COMM_WORLD.Recv(receivedMM, 0, N[0]*N[0], MPI.INT, 4, 0);

                int [][] MC_5H_6H =  Resources.rowToMatrix(receivedMC_5H_6H, 0, N[0], H);
                int [][] MS_5H_6H =  Resources.rowToMatrix(receivedMS_5H_6H, 0, N[0], H);
                int [][] MR_6 =  Resources.rowToMatrix(receivedMR, 0, N[0], N[0]);
                int [][] MM_6 =  Resources.rowToMatrix(receivedMM, 0, N[0], N[0]);

                System.out.println("T6: I got everything!");

                //Обчислення 1: p6 = max(Rh)
                int [] p6 = {Resources.findMax_mi(receivedR_5H_6H, 0, H)};

                //Передати до Т5 p6
                MPI.COMM_WORLD.Send(p6, 0, 1, MPI.INT, 4, 0);

                //Прийняти від T3 p
                int [] p = new int[1];
                MPI.COMM_WORLD.Recv(p, 0, 1, MPI.INT, 2, 0);
                System.out.println("[T6]: I got p: " + p[0]);

                //Передати до Т8 p
                MPI.COMM_WORLD.Send(p, 0, 1, MPI.INT, 7, 0);

                //Обчислення 3: MZh = MM*(MR*MCh) - p*MSh
                int [][] MZ_5H_6H = new int[N[0]][H];

                Resources.multiplyScalarOnMatrixColumns(MS_5H_6H, p[0], 0, H);
                Resources.computePartialResult(MM_6, MR_6, MC_5H_6H, MZ_5H_6H, 0, H);
                Resources.subtractMatricesByColumns(MZ_5H_6H, MS_5H_6H, 0, H);

                int [] sendMZ_5H_6H = Resources.matrixToColumnNotSq(MZ_5H_6H, N[0], H, 0);

                // Передати до Т5 MZ[5h..6h]
                MPI.COMM_WORLD.Send(sendMZ_5H_6H, 0, H*N[0], MPI.INT, 4, 0);

                break;
            }
            case 6: {
                //Прийняти від Т5 R[6h..7h][7h..8h], MC[6h..7h][7h..8h], MS[6h..7h][7h..8h], MR, MM
                int[] receivedR_6H_8H = new int[2*H];

                int[] receivedMC_6H_8H = new int[2*H*N[0]];
                int[] receivedMS_6H_8H = new int[2*H*N[0]];

                int[] receivedMM = new int[N[0]*N[0]];
                int[] receivedMR = new int[N[0]*N[0]];

                MPI.COMM_WORLD.Recv(receivedR_6H_8H, 0, 2*H, MPI.INT, 4, 0);

                MPI.COMM_WORLD.Recv(receivedMC_6H_8H, 0, 2*H*N[0], MPI.INT, 4, 0);
                MPI.COMM_WORLD.Recv(receivedMS_6H_8H, 0, 2*H*N[0], MPI.INT, 4, 0);

                MPI.COMM_WORLD.Recv(receivedMR, 0, N[0]*N[0], MPI.INT, 4, 0);
                MPI.COMM_WORLD.Recv(receivedMM, 0, N[0]*N[0], MPI.INT, 4, 0);

                int [][] MC_6H_7H =  Resources.rowToMatrix(receivedMC_6H_8H, 0, N[0], H);
                int [][] MS_6H_7H =  Resources.rowToMatrix(receivedMS_6H_8H, 0, N[0], H);
                int [][] MR_7 =  Resources.rowToMatrix(receivedMR, 0, N[0], N[0]);
                int [][] MM_7 =  Resources.rowToMatrix(receivedMM, 0, N[0], N[0]);

                System.out.println("T7: I got everything!");

                // Передати R[7h..8h], MC[7h..8h], MS[7h..8h], MR, MM до T8
                MPI.COMM_WORLD.Send(receivedR_6H_8H, H, H, MPI.INT, 7, 0);

                MPI.COMM_WORLD.Send(receivedMC_6H_8H, H*N[0], H*N[0], MPI.INT, 7, 0);
                MPI.COMM_WORLD.Send(receivedMS_6H_8H, H*N[0], H*N[0], MPI.INT, 7, 0);

                MPI.COMM_WORLD.Send(receivedMR, 0, N[0]*N[0], MPI.INT, 7, 0);
                MPI.COMM_WORLD.Send(receivedMM, 0, N[0]*N[0], MPI.INT, 7, 0);

                //Обчислення 1: p7 = max(Rh)
                int [] p7 = {Resources.findMax_mi(receivedR_6H_8H, 0, H)};

                //Прийняти від T8 p8
                int [] p8 = new int [1];
                MPI.COMM_WORLD.Recv(p8, 0, 1, MPI.INT, 7, 0);

                //Обчислення 2: p7 = max(p7, p8)
                p7[0] = Math.max(p7[0], p8[0]);

                //Передати до Т5 p7
                MPI.COMM_WORLD.Send(p7, 0, 1, MPI.INT, 4, 0);

                //Прийняти від T2 p
                int [] p = new int[1];
                MPI.COMM_WORLD.Recv(p, 0, 1, MPI.INT, 1, 0);
                System.out.println("[T7]: I got p: " + p[0]);

                int [][] MZ_6H_7H = new int[N[0]][H];
                //Обчислення 3: MZh = MM*(MR*MCh) - p*MSh
                Resources.multiplyScalarOnMatrixColumns(MS_6H_7H, p[0], 0, H);
                Resources.computePartialResult(MM_7, MR_7, MC_6H_7H, MZ_6H_7H, 0, H);
                Resources.subtractMatricesByColumns(MZ_6H_7H, MS_6H_7H, 0, H);

                int [] sendMZ_6H_8H = Resources.matrixToColumnNotSq(MZ_6H_7H, N[0], 2*H, 0);
                //Прийняти від Т8 MZ[7h..8h]
                MPI.COMM_WORLD.Recv(sendMZ_6H_8H, H*N[0], H*N[0], MPI.INT, 7, 0);

                //Передати до Т5 MZ[6h..7h][7h..8h]
                MPI.COMM_WORLD.Send(sendMZ_6H_8H, 0, 2*H*N[0], MPI.INT, 4, 0);

                break;
            }
            case 7: {
                //Прийняти від Т7 R[7h..8h], MC[7h..8h], MS[7h..8h], MR, MM
                int[] receivedR_7H_8H = new int[H];

                int[] receivedMC_7H_8H = new int[H*N[0]];
                int[] receivedMS_7H_8H = new int[H*N[0]];

                int[] receivedMM = new int[N[0]*N[0]];
                int[] receivedMR = new int[N[0]*N[0]];

                MPI.COMM_WORLD.Recv(receivedR_7H_8H, 0, H, MPI.INT, 6, 0);

                MPI.COMM_WORLD.Recv(receivedMC_7H_8H, 0, H*N[0], MPI.INT, 6, 0);
                MPI.COMM_WORLD.Recv(receivedMS_7H_8H, 0, H*N[0], MPI.INT, 6, 0);

                MPI.COMM_WORLD.Recv(receivedMR, 0, N[0]*N[0], MPI.INT, 6, 0);
                MPI.COMM_WORLD.Recv(receivedMM, 0, N[0]*N[0], MPI.INT, 6, 0);

                int [][] MC_7H_8H =  Resources.rowToMatrix(receivedMC_7H_8H, 0, N[0], H);
                int [][] MS_7H_8H =  Resources.rowToMatrix(receivedMS_7H_8H, 0, N[0], H);
                int [][] MR_8 =  Resources.rowToMatrix(receivedMR, 0, N[0], N[0]);
                int [][] MM_8 =  Resources.rowToMatrix(receivedMM, 0, N[0], N[0]);

                System.out.println("T8: I got everything!");

                //Обчислення 1: p8 = max(Rh)
                int []p8 = {Resources.findMax_mi(receivedR_7H_8H, 0, H)};

                //Передати до Т7 p8
                MPI.COMM_WORLD.Send(p8, 0, 1, MPI.INT, 6, 0);

                //Прийняти від T6 p
                int [] p = new int[1];
                MPI.COMM_WORLD.Recv(p, 0, 1, MPI.INT, 5, 0);
                System.out.println("[T8]: I got p: " + p[0]);


                //Обчислення 3: MZh = MM*(MR*MCh) - p*MSh
                int [][] MZ_7H_8H = new int[N[0]][H];

                Resources.multiplyScalarOnMatrixColumns(MS_7H_8H, p[0], 0, H);
                Resources.computePartialResult(MM_8, MR_8, MC_7H_8H, MZ_7H_8H, 0, H);
                Resources.subtractMatricesByColumns(MZ_7H_8H, MS_7H_8H, 0, H);

                int []sendMZ_7H_8H = Resources.matrixToColumnNotSq(MZ_7H_8H, N[0], H, 0);
                MPI.COMM_WORLD.Send(sendMZ_7H_8H, 0, H*N[0], MPI.INT, 6, 0);

                break;
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Thread" + (rank + 1) + " is finished! Time: " + (endTime - startTime) + " ms");
    }
}
