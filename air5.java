import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;

import ilog.concert.*;
import ilog.cplex.*;






public class air5 {
	
	
	
	public static MiniPath[] GetColumn(int[][][] timeGraph, int timeFrom, int locationFrom, int locNum, int tmax) {
		MiniPath [] path = new MiniPath[tmax];
		int[][] dynArr = new int[locNum][tmax+1];
		int currTime = timeFrom;
		int nextTime = currTime+1;
		if (nextTime > tmax) {
			nextTime = 1;//=0?
		}
		for (int i = 0; i < locNum; i++) {			
			dynArr[i][nextTime] = timeGraph[currTime][locationFrom][i];
		}
		currTime = nextTime;
		while (currTime!=timeFrom) {
			nextTime = currTime+1;
			if (nextTime > tmax) {
				nextTime = 1;//=0?
			}			
			if (nextTime!=timeFrom) {
				for (int i = 0; i < locNum; i++) {
					int min = 1000*tmax;
					for (int j = 0; j<locNum; j++) {
						if (dynArr[j][currTime]+timeGraph[currTime][j][i] < min) {
							min = dynArr[j][currTime]+timeGraph[currTime][j][i];
						}
					}				
					dynArr[i][nextTime] = min;
				}			
			}
			else {
				int i = locationFrom;
				int min = 0;
				for (int j = 0; j<locNum; j++) {
					if (dynArr[j][currTime]+timeGraph[currTime][j][i] < min) {
						min = dynArr[j][currTime]+timeGraph[currTime][j][i];
					}
				}				
				dynArr[i][nextTime] = min;
			}
			currTime = nextTime;
		}
		int pathLen = dynArr[locationFrom][timeFrom];
		int currLoc = locationFrom;
		int preTime = currTime -1;
		if (preTime <= 0) { //preTime < 0
			preTime = tmax;
		}
		int currMiniPath = tmax - 1;
		while (preTime!=timeFrom) {
			int i1 = -1;
			for (int i = 0; i < locNum; i++) {
				if (dynArr[i][preTime]+timeGraph[preTime][i][currLoc]==dynArr[currLoc][currTime]) {
					i1 = i;
					break;
				}
			}
			MiniPath currPath = new MiniPath(i1, currLoc, preTime);
			path[currMiniPath] = currPath;
			currMiniPath--;
			currLoc = i1;
			currTime = preTime;
			preTime = currTime -1;
			if (preTime <= 0) { //preTime < 0
				preTime = tmax;
			}
		}		
		MiniPath currPath = new MiniPath(locationFrom, currLoc, timeFrom);
		path[currMiniPath] = currPath;
		return path;
	}
	
	
	public static void main(String[] args) {
		try {
			
			//read from file
			File testFile = new File("C://My Files//TestData//airschedule.txt");
			FileReader fr = new FileReader(testFile);
			Scanner scan = new Scanner(fr);
			int tmax = scan.nextInt();
			int totalCityNum = scan.nextInt();
			String temp = scan.nextLine();
			temp = scan.nextLine();
			String [] cityNames = new String [20];			
			int [][][] timeGraph = new int[tmax+1][totalCityNum+1][totalCityNum+1];
			int [][][] copyTimeGraph = new int[tmax+1][totalCityNum+1][totalCityNum+1];
			ArrayList<Integer> [][][] routesInfo = new ArrayList[tmax+1][totalCityNum+1][totalCityNum+1];
			for (int i = 0; i <=tmax; i++) {
				for (int j = 0; j<=totalCityNum; j++) {
					for (int k = 0; k <= totalCityNum; k++) {
						routesInfo[i][j][k] = new ArrayList<Integer>();
					}
				}
			}
			int cityNum = 0;
			int constrNum = 0;
			while (scan.hasNextInt()) {
				int route = scan.nextInt();
				
				String data = scan.nextLine();
				while(data.charAt(0)=='\t') {
					data = data.substring(1);
				}
				String from = "";
				while(data.charAt(0)!='\t') {
					from = from + data.charAt(0);
					data = data.substring(1);
				}
				while(data.charAt(0)=='\t') {
					data = data.substring(1);
				}
				String to = "";
				while(data.charAt(0)!='\t') {
					to = to + data.charAt(0);
					data = data.substring(1);
				}
				while(data.charAt(0)=='\t') {
					data = data.substring(1);
				}
				int len = data.length();
				len = len/2;	
				boolean b = true;
				if (data.charAt(data.length()-1)<49) {
					b = false;
				}
				else {
					len = len +1;
				}
				int[] days = new int[len];
				int i = 0;
				while(data.length() > 1) {
					days[i] = data.charAt(0)-48;
					data = data.substring(2);
					i++;
				}				
				if (b) 
				{
					days[i] = data.charAt(0)-48;
				}	
				int i1 = -1;
				int j1 = -1;
				for (i = 0; i < cityNum; i++) {
					if (cityNames[i].equals(from)) {
						i1 = i;
					}
					if (cityNames[i].equals(to)) {
						j1 = i;
					}
					if (i1!=-1 && j1!=-1) {
						break;
					}
				}
				if (i1 == -1) {
					cityNames[cityNum] = from;					
					i1 = cityNum;
					cityNum++;
					if (from == to) {
						j1 = i1;
					}
				}
				if (j1 == -1) {
					cityNames[cityNum] = to;
					j1 = cityNum;
					cityNum++;					
				}
				for (i = 0; i < len; i++) {
					if (timeGraph[days[i]][i1][j1]==0) {
						constrNum++;
					}
					timeGraph[days[i]][i1][j1]-=1;	
					copyTimeGraph[days[i]][i1][j1] = timeGraph[days[i]][i1][j1]*2;
					routesInfo[days[i]][i1][j1].add(route);				
					
				}			
			}
			for (int i = 0; i <=tmax; i++) {
				for (int j = 0; j<=totalCityNum; j++) {
					for (int k = 0; k <= totalCityNum; k++) {
						if (copyTimeGraph[i][j][k]==0) {
							if (j==k) {
								copyTimeGraph[i][j][k] = -1;								
							}
							else {
								copyTimeGraph[i][j][k] = 1000;
							}
						}
					}
				}
			}
			//end read
			
			
					
			
			
			//Initial columns:
			ArrayList<Integer> [][][] columnList = new ArrayList[tmax+1][totalCityNum+1][totalCityNum+1];
			for (int i = 0; i <=tmax; i++) {
				for (int j = 0; j<=totalCityNum; j++) {
					for (int k = 0; k <= totalCityNum; k++) {
						columnList[i][j][k] = new ArrayList<Integer>();
					}
				}
			}
			int colNum = 0;
			ArrayList<MiniPath []> pathes = new ArrayList<MiniPath []>();
			for (int l = 0; l < totalCityNum; l++) {
				//for (int t = 1; t <= tmax; t++) {
					int pathLen = -tmax-1;
					while (pathLen < -tmax) {
						MiniPath[] path = GetColumn(copyTimeGraph, 1, l, totalCityNum, tmax);
						pathLen = 0;
						for (int i1 = 0; i1 < tmax; i1++) {
							pathLen += copyTimeGraph[path[i1].routeDay][path[i1].from][path[i1].to];
						}
						if (pathLen < -tmax) {
							pathes.add(path);
							for (int i1 = 0; i1 < tmax; i1++) {
								columnList[path[i1].routeDay][path[i1].from][path[i1].to].add(colNum);
								copyTimeGraph[path[i1].routeDay][path[i1].from][path[i1].to] = -1;
							}
							colNum++;
						}
					}
				//}
			}
			//Initial column finished
			
			
			
			
			
			
			
			
			//Master LR problem:
			try {
				boolean finish = false;
				while(!finish) {
					IloCplex masterCplex = new IloCplex();
					double[] lb = new double[colNum];
					Arrays.fill(lb, 0.0);
					double[] ub = new double[colNum];
					Arrays.fill(ub, Double.MAX_VALUE);
					IloNumVarType[] xt = new IloNumVarType[colNum];
					Arrays.fill(xt, IloNumVarType.Float); 
					IloNumVar[] x = masterCplex.numVarArray(colNum, lb, ub, xt);
					double[] objvals = new double[colNum];
					Arrays.fill(objvals, 1.0);
					masterCplex.addMinimize(masterCplex.scalProd(x, objvals));
					IloRange[] rng = new IloRange[constrNum]; 
					int[][] constrInfo = new int[constrNum][3];
					int j1 = 0;
					
					for (int i = 0; i <=tmax; i++) {
						for (int j = 0; j<=totalCityNum; j++) {
							for (int k = 0; k <= totalCityNum; k++) {
								int colSize = columnList[i][j][k].size();
								if (timeGraph[i][j][k] < 0) {
									IloLinearNumExpr expr = masterCplex.linearNumExpr();
									for (int i1 = 0; i1 < colSize; i1++) {
										int thisColNum = columnList[i][j][k].get(i1);
										expr.addTerm(1.0, x[thisColNum]);
									}
									rng[j1] =  masterCplex.addGe(expr, -timeGraph[i][j][k], "c"+String.valueOf(j1));
									constrInfo[j1][0] = i;
									constrInfo[j1][1] = j;
									constrInfo[j1][2] = k;
									j1++;
								}
							}
						}
					}		
					
					masterCplex.solve();				
					double[] pi = masterCplex.getDuals(rng);
					double newObj = masterCplex.getObjValue();
					System.out.println(newObj);
					masterCplex.end();
					//End of master LRP
					
					
					
					
					
					//Column generation
					for (int i = 0; i <=tmax; i++) {
						for (int j = 0; j<=totalCityNum; j++) {
							for (int k = 0; k <= totalCityNum; k++) {
								if(copyTimeGraph[i][j][k] != 1000) {
									copyTimeGraph[i][j][k] = 0;
								}
							}
						}
					}
					for (int i = 0; i < constrNum; i++) {
						
							copyTimeGraph[constrInfo[i][0]][constrInfo[i][1]][constrInfo[i][2]] = -(int)Math.round(pi[i])*2;
						
					}
					int delta = colNum;
					
					
					
					for (int l = 0; l < totalCityNum; l++) {
						for (int t = 1; t <= tmax; t++) {
							int pathLen = -tmax-1;
							while (pathLen < -tmax) {
								MiniPath[] path = GetColumn(copyTimeGraph, t, l, totalCityNum, tmax);
								pathLen = 0;
								for (int i1 = 0; i1 < tmax; i1++) {
									pathLen += copyTimeGraph[path[i1].routeDay][path[i1].from][path[i1].to];
								}
								if (pathLen < -tmax) {
									pathes.add(path);
									for (int i1 = 0; i1 < tmax; i1++) {
										columnList[path[i1].routeDay][path[i1].from][path[i1].to].add(colNum);
										copyTimeGraph[path[i1].routeDay][path[i1].from][path[i1].to] = -1;
									}
									colNum++;
									//System.err.println(colNum);
								}
							}
						}
					}
					
					
					delta = colNum - delta;
					if (delta==0) {
						finish = true;
					}
					// End of Column generation
					
					
					
					
					
					
				}
				//System.out.println(colNum);
				
				
				
				
				//Master problem IP
				IloCplex masterCplex = new IloCplex();
				double[] lb = new double[colNum];
				Arrays.fill(lb, 0.0);
				double[] ub = new double[colNum];
				Arrays.fill(ub, Double.MAX_VALUE);
				IloNumVarType[] xt = new IloNumVarType[colNum];
				Arrays.fill(xt, IloNumVarType.Int); 
				IloNumVar[] x = masterCplex.numVarArray(colNum, lb, ub, xt);
				double[] objvals = new double[colNum];
				Arrays.fill(objvals, 1.0);
				masterCplex.addMinimize(masterCplex.scalProd(x, objvals));
				IloRange[] rng = new IloRange[constrNum]; 
				int[][] constrInfo = new int[constrNum][3];
				int j1 = 0;
				
				for (int i = 0; i <=tmax; i++) {
					for (int j = 0; j<=totalCityNum; j++) {
						for (int k = 0; k <= totalCityNum; k++) {
							int colSize = columnList[i][j][k].size();
							if (timeGraph[i][j][k] < 0) {
								IloLinearNumExpr expr = masterCplex.linearNumExpr();
								for (int i1 = 0; i1 < colSize; i1++) {
									int thisColNum = columnList[i][j][k].get(i1);
									expr.addTerm(1.0, x[thisColNum]);
								}
								rng[j1] =  masterCplex.addGe(expr, -timeGraph[i][j][k], "c"+String.valueOf(j1));
								constrInfo[j1][0] = i;
								constrInfo[j1][1] = j;
								constrInfo[j1][2] = k;
								j1++;
							}
						}
					}
				}		
				
				masterCplex.solve();				
				double newObj = masterCplex.getObjValue();
				System.out.println(newObj);
				double[] optValues = masterCplex.getValues(x);
				int oldColNum = colNum;
				masterCplex.end();
				//Solved
				//Solution time around one second (maybe several seconds)
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				int[] newOpt = new int[oldColNum*10];
				
				/*for (int i = 0; i < colNum; i++) {
					if (optValues[i]>0) {
						System.out.print(i+" "+optValues[i]+"     ");
					}
					
				}*/
				ArrayList<Integer> [][][] copyRoutesInfo = new ArrayList[tmax+1][totalCityNum+1][totalCityNum+1];
				for (int i = 0; i <=tmax; i++) {
					for (int j = 0; j<=totalCityNum; j++) {
						for (int k = 0; k <= totalCityNum; k++) {
							copyRoutesInfo[i][j][k] = new ArrayList<Integer>();
						}
					}
				}
				
				
				for (int i = 0; i < oldColNum; i++) {
					int opt = (int)Math.round(optValues[i]);
					MiniPath [] path = pathes.get(i);
					while (opt > 0) {
						int [] withOtherFlight = new int[tmax];
						for (int i1 = 0; i1 < tmax; i1++) {
							if (routesInfo[path[i1].routeDay][path[i1].from][path[i1].to].size()==0) {
								if (path[i1].from == path[i1].to) {
									
								}
								else {
									withOtherFlight[i1] = 1;
								}
							}
							else {
								int route = routesInfo[path[i1].routeDay][path[i1].from][path[i1].to].get(0);
								routesInfo[path[i1].routeDay][path[i1].from][path[i1].to].remove(0);
								copyRoutesInfo[path[i1].routeDay][path[i1].from][path[i1].to].add(route);
							}
						}
						
						MiniPath [] newPath = new MiniPath[tmax];
						for (int i1 = 0; i1 < tmax; i1++) {
							newPath[i1] = new MiniPath(path[i1].from, path[i1].to, path[i1].routeDay); 
						}
						boolean needAddNewPath = false;
						int i1 = 0;
						while (i1 < tmax) {							
							int nextIndex = i1 + 1;
							if (nextIndex>=tmax) {
								nextIndex -= tmax; 
							}
							if ((newPath[i1].to == newPath[nextIndex].from) && (newPath[i1].from == newPath[nextIndex].to) && (withOtherFlight[i1] == 1) && (withOtherFlight[nextIndex] == 1)) {
								needAddNewPath = true;
								newPath[i1] = new MiniPath(path[i1].from, path[i1].from, path[i1].routeDay);
								newPath[nextIndex] = new MiniPath(path[i1].from, path[i1].from, path[nextIndex].routeDay);
							}
							i1++;
						}
						if (needAddNewPath) {
							optValues[i] -= 1;
							newOpt[colNum - oldColNum] = 1;
							pathes.add(newPath);
							for (int j = 0; j < tmax; j++) {
								columnList[newPath[j].routeDay][newPath[j].from][newPath[j].to].add(colNum);
								copyTimeGraph[newPath[j].routeDay][newPath[j].from][newPath[j].to] = -1;
							}
							colNum++;
						}						
						opt--;
					}
					
				}
				
				
				
				
				System.out.println();
				long newColMax = 0;
				long[][] stayAt = new long[tmax+1][totalCityNum+1];
				long[][][]goWithOther = new long[tmax+1][totalCityNum+1][totalCityNum+1];
				for (int i = 0; i <=tmax; i++) {
					for (int j = 0; j<=totalCityNum; j++) {
						for (int k = 0; k <= totalCityNum; k++) {
							if (columnList[i][j][k].size()>0) {								
								long sumOfGroups = 0;
								for (int i1 = 0; i1<columnList[i][j][k].size(); i1++) {
									int col = columnList[i][j][k].get(i1);									
									try {
										if (optValues[col]>0) {
											//System.out.print(col+" ");
											sumOfGroups = sumOfGroups + Math.round(optValues[col]);
										}
									}
									catch (ArrayIndexOutOfBoundsException e) {
										if (newOpt[col-oldColNum]>0) {
											//System.out.print(col+" ");
											sumOfGroups = sumOfGroups + Math.round(newOpt[col-oldColNum]);
										}
									}
								}
								
								
								
								
								if (sumOfGroups+timeGraph[i][j][k]>0) {
									if (j==k) {
										stayAt[i][j] = sumOfGroups+timeGraph[i][j][k];
									}
									else {
										goWithOther[i][j][k] = sumOfGroups + timeGraph[i][j][k];
										newColMax = newColMax + sumOfGroups + timeGraph[i][j][k];
									}
								}
							
							
							
							
							
							}
						}
					}
				}
				//double[] newOpt = new double[colNum*10]; 
				for (int i = 0; i <=tmax; i++) {
					for (int j = 0; j<=totalCityNum; j++) {
						for (int k = 0; k <= totalCityNum; k++) {							
							if (goWithOther[i][j][k] > 0 && goWithOther[i][k][j] > 0) {
								
								long min = goWithOther[i][j][k];
								if (min > goWithOther[i][k][j]) {
									min = goWithOther[i][k][j];
								}
								
								int tempIndex1 = 0;								
								long tempMin = min;
								boolean removedAll = true;
								while ((tempMin > 0) && removedAll) {
									int col1 = -1;
									int col2 = -1;
									int size1 = columnList[i][j][k].size();
									int size2 = columnList[i][k][j].size();
									boolean notZero = false;
									while (!notZero && (tempIndex1 < size1)) {
										boolean foundPair = true;
										col1 = columnList[i][j][k].get(tempIndex1);
										int val = 0;
										if (col1<oldColNum) {
											val = (int) Math.round(optValues[col1]);
										}
										else {
											val = (int) Math.round(newOpt[col1-oldColNum]);
										}
										if (val > 0) {
											notZero = true;
											MiniPath [] oldPath1 = pathes.get(col1);
											foundPair = false;
											int tempIndex2 = 0;
											while (!foundPair && (tempIndex2 < size2)) {
												col2 = columnList[i][k][j].get(tempIndex2);
												val = 0;
												if (col2<oldColNum) {
													val = (int) Math.round(optValues[col2]);
												}
												else {
													val = (int) Math.round(newOpt[col2-oldColNum]);
												}
												if ((val > 0) && (pathes.get(col2)[0].from == oldPath1[0].from) && (pathes.get(col2)[0].routeDay == oldPath1[0].routeDay)) {
													foundPair = true;													
												}
												else {
													tempIndex2++;
												}
											}
											if (!foundPair) {
												tempIndex1++;
											}
										}
										else {
											tempIndex1++;
										}
										notZero = notZero && foundPair;
									}
									if (notZero) {
										MiniPath [] oldPath1 = pathes.get(col1);
										MiniPath [] oldPath2 = pathes.get(col2);
										MiniPath [] path1 = new MiniPath[tmax];
										MiniPath [] path2 = new MiniPath[tmax];
										if (col1<oldColNum) {
											optValues[col1]--;
										}
										else {
											newOpt[col1-oldColNum]--;
										}
										if (col2<oldColNum) {
											optValues[col2]--;
										}
										else {
											newOpt[col2-oldColNum]--;
										}
										boolean beforeChangingPlace = true;
										for (int i1 = 0; i1 < tmax; i1++) {
											if (oldPath1[i1].routeDay != i ) {
												if (beforeChangingPlace) {
													path1[i1] = new MiniPath(oldPath1[i1].from, oldPath1[i1].to, oldPath1[i1].routeDay);
													path2[i1] = new MiniPath(oldPath2[i1].from, oldPath2[i1].to, oldPath2[i1].routeDay);
												}
												else {
													path2[i1] = new MiniPath(oldPath1[i1].from, oldPath1[i1].to, oldPath1[i1].routeDay);
													path1[i1] = new MiniPath(oldPath2[i1].from, oldPath2[i1].to, oldPath2[i1].routeDay);
												}
											}
											else {
												beforeChangingPlace = false;
												path1[i1] = new MiniPath(oldPath1[i1].from, oldPath1[i1].from, oldPath1[i1].routeDay);
												path2[i1] = new MiniPath(oldPath2[i1].from, oldPath2[i1].from, oldPath2[i1].routeDay);
											}
										}
										pathes.add(path1);										
										for (int i1 = 0; i1 < tmax; i1++) {
											columnList[path1[i1].routeDay][path1[i1].from][path1[i1].to].add(colNum);											
										}
										newOpt[colNum-oldColNum] = 1;
										colNum++;										
										pathes.add(path2);										
										for (int i1 = 0; i1 < tmax; i1++) {
											columnList[path2[i1].routeDay][path2[i1].from][path2[i1].to].add(colNum);											
										}
										newOpt[colNum-oldColNum] = 1;
										colNum++;										
										tempMin--;
									}
									else {
										removedAll = false;
									}									
								}
								goWithOther[i][j][k] -= (min-tempMin);
								goWithOther[i][k][j] -= (min-tempMin);								
							}
							
							int nextTime = i+1;
							if (nextTime > tmax) {
								nextTime = 1;
							}
							if (goWithOther[i][j][k] > 0 && goWithOther[nextTime][k][j] > 0) {
								long min = goWithOther[i][j][k];
								if (min > goWithOther[nextTime][k][j]) {
									min = goWithOther[nextTime][k][j];
								}
								int tempIndex1 = 0;								
								long tempMin = min;
								boolean removedAll = true;
								while ((tempMin > 0) && removedAll) {
									int col1 = -1;
									int col2 = -1;
									int size1 = columnList[i][j][k].size();
									int size2 = columnList[nextTime][k][j].size();
									boolean notZero = false;
									while (!notZero && (tempIndex1 < size1)) {
										boolean foundPair = true;
										col1 = columnList[i][j][k].get(tempIndex1);
										int val = 0;
										if (col1<oldColNum) {
											val = (int) Math.round(optValues[col1]);
										}
										else {
											val = (int) Math.round(newOpt[col1-oldColNum]);
										}
										if (val > 0) {
											notZero = true;
											MiniPath [] oldPath1 = pathes.get(col1);
											foundPair = false;
											if (oldPath1[tmax-1].routeDay != i) {
												int tempIndex2 = 0;
												while (!foundPair && (tempIndex2 < size2)) {
													col2 = columnList[nextTime][k][j].get(tempIndex2);
													val = 0;
													if (col2<oldColNum) {
														val = (int) Math.round(optValues[col2]);
													}
													else {
														val = (int) Math.round(newOpt[col2-oldColNum]);
													}
													if ((val > 0) && (pathes.get(col2)[0].from == oldPath1[0].from) && (pathes.get(col2)[0].routeDay == oldPath1[0].routeDay) /*&& (col1!=col2)*/) {
														foundPair = true;													
													}
													else {
														tempIndex2++;
													}
												}
											}
											if (!foundPair) {
												tempIndex1++;
											}
										}
										else {
											tempIndex1++;
										}
										notZero = notZero && foundPair;
									}
									if (notZero) {
										if (col1!=col2) {
											MiniPath [] oldPath1 = pathes.get(col1);
											MiniPath [] oldPath2 = pathes.get(col2);
											MiniPath [] path1 = new MiniPath[tmax];
											MiniPath [] path2 = new MiniPath[tmax];
											if (col1<oldColNum) {
												optValues[col1]--;
											}
											else {
												newOpt[col1-oldColNum]--;
											}
											if (col2<oldColNum) {
												optValues[col2]--;
											}
											else {
												newOpt[col2-oldColNum]--;
											}										
											boolean beforeChangingPlace = true;
											boolean skipDay = false;
											for (int i1 = 0; i1 < tmax; i1++) {
												if(!skipDay) {
													if (oldPath1[i1].routeDay != i ) {
														if (beforeChangingPlace) {
															path1[i1] = new MiniPath(oldPath1[i1].from, oldPath1[i1].to, oldPath1[i1].routeDay);
															path2[i1] = new MiniPath(oldPath2[i1].from, oldPath2[i1].to, oldPath2[i1].routeDay);
														}
														else {
															path2[i1] = new MiniPath(oldPath1[i1].from, oldPath1[i1].to, oldPath1[i1].routeDay);
															path1[i1] = new MiniPath(oldPath2[i1].from, oldPath2[i1].to, oldPath2[i1].routeDay);
														}
													}
													else {
														beforeChangingPlace = false;
														path1[i1] = new MiniPath(oldPath1[i1].from, oldPath1[i1].from, oldPath1[i1].routeDay);
														path2[i1] = new MiniPath(oldPath2[i1].from, oldPath2[i1].to, oldPath2[i1].routeDay);
														path1[i1+1] = new MiniPath(oldPath1[i1].from, oldPath1[i1].from, oldPath1[i1+1].routeDay);
														path2[i1+1] = new MiniPath(oldPath1[i1+1].from, oldPath1[i1+1].to, oldPath1[i1+1].routeDay);
														skipDay = true;
													}
												}
												else {
													skipDay = false;
												}
											}
											pathes.add(path1);										
											for (int i1 = 0; i1 < tmax; i1++) {
												columnList[path1[i1].routeDay][path1[i1].from][path1[i1].to].add(colNum);											
											}
											newOpt[colNum-oldColNum] = 1;
											colNum++;										
											pathes.add(path2);										
											for (int i1 = 0; i1 < tmax; i1++) {
												columnList[path2[i1].routeDay][path2[i1].from][path2[i1].to].add(colNum);											
											}
											newOpt[colNum-oldColNum] = 1;
											colNum++;										
											tempMin--;
										}
										else {
											MiniPath [] oldPath = pathes.get(col1);
											MiniPath [] path = new MiniPath[tmax];
											if (col1<oldColNum) {
												optValues[col1]--;
											}
											else {
												newOpt[col1-oldColNum]--;
											}
											for (int i1 = 0; i1 < tmax; i1++) {
												if (oldPath[i1].routeDay == i ) {
													path[i1] = new MiniPath(oldPath[i1].from, oldPath[i1].from, oldPath[i1].routeDay);
												}
												else {
													if ((i1 > 0) && path[i1-1].to!=oldPath[i1].from) {
														path[i1] = new MiniPath(oldPath[i1].to, oldPath[i1].to, oldPath[i1].routeDay);
													}
													else {
														path[i1] = new MiniPath(oldPath[i1].from, oldPath[i1].to, oldPath[i1].routeDay);
													}
												}
											}
											pathes.add(path);	
											for (int i1 = 0; i1 < tmax; i1++) {
												columnList[path[i1].routeDay][path[i1].from][path[i1].to].add(colNum);
											}
											newOpt[colNum-oldColNum] = 1;
											colNum++;	
											tempMin--;
											
											
										}
									}
									else {
										removedAll = false;
									}									
									
								}
								
								
								goWithOther[i][j][k] -= min;
								goWithOther[nextTime][k][j] -= min;
								
								
								
							}	
							
							
						}
					}
				}
				
				
				
				
				
				
				
				
				
				
				int groupNumber = 0;
				for (int i = 0; i < oldColNum; i++) {
					int opt = (int)Math.round(optValues[i]);
					MiniPath [] path = pathes.get(i);
					while (opt > 0) {
						groupNumber++;
						System.out.println("Crew group " + groupNumber + " schedule: ");
						System.out.print("Starting from ");
						for (int i1 = 0; i1 < tmax; i1++) {
							System.out.print("Day " + path[i1].routeDay + " from " + cityNames[path[i1].from] + " to " + cityNames[path[i1].to]);
							if (copyRoutesInfo[path[i1].routeDay][path[i1].from][path[i1].to].size()==0) {
								if (path[i1].from == path[i1].to) {
									System.out.println(" (rest);");
								}
								else {
									System.out.println(" (with other flight);");
								}
							}
							else {
								int route = copyRoutesInfo[path[i1].routeDay][path[i1].from][path[i1].to].get(0);
								System.out.println(" (route " + route + ");");
								copyRoutesInfo[path[i1].routeDay][path[i1].from][path[i1].to].remove(0);
								routesInfo[path[i1].routeDay][path[i1].from][path[i1].to].add(route);
							}
						}						
						opt--;
						System.out.println();
					}
					
				}
				for (int i = 0; i < (colNum - oldColNum); i++) {
					int opt = (int)Math.round(newOpt[i]);
					MiniPath [] path = pathes.get(i+oldColNum);
					while (opt > 0) {
						groupNumber++;
						System.out.println("Crew group " + groupNumber + " schedule: ");
						System.out.print("Starting from ");
						for (int i1 = 0; i1 < tmax; i1++) {
							System.out.print("Day " + path[i1].routeDay + " from " + cityNames[path[i1].from] + " to " + cityNames[path[i1].to]);
							if (copyRoutesInfo[path[i1].routeDay][path[i1].from][path[i1].to].size()==0) {
								if (path[i1].from == path[i1].to) {
									System.out.println(" (rest);");
								}
								else {
									System.out.println(" (with other flight);");
								}
							}
							else {
								int route = copyRoutesInfo[path[i1].routeDay][path[i1].from][path[i1].to].get(0);
								System.out.println(" (route " + route + ");");
								copyRoutesInfo[path[i1].routeDay][path[i1].from][path[i1].to].remove(0);
								routesInfo[path[i1].routeDay][path[i1].from][path[i1].to].add(route);
							}
						}						
						opt--;
						System.out.println();
					}
					
				}
				
				
				
				
			}
			catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}
		catch (IOException e) {
			System.err.println("File exception caught: " + e);
		}	
	}
	
}