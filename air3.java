import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;

import ilog.concert.*;
import ilog.cplex.*;






public class air3 {
	
	
	
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
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				/*for (int i = 0; i < colNum; i++) {
					if (optValues[i]>0) {
						System.out.print(i+" "+optValues[i]+"     ");
					}
					
				}*/
				
				
				
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
							if (routesInfo[path[i1].routeDay][path[i1].from][path[i1].to].size()==0) {
								if (path[i1].from == path[i1].to) {
									System.out.println(" (rest);");
								}
								else {
									System.out.println(" (with other flight);");
								}
							}
							else {
								int route = routesInfo[path[i1].routeDay][path[i1].from][path[i1].to].get(0);
								System.out.println(" (route " + route + ");");
								routesInfo[path[i1].routeDay][path[i1].from][path[i1].to].remove(0);
							}
						}						
						opt--;
						System.out.println();
					}
					
				}
				/*for (int i = 0; i < (colNum - oldColNum); i++) {
					int opt = (int)Math.round(newOpt[i]);
					MiniPath [] path = pathes.get(i);
					while (opt > 0) {
						groupNumber++;
						System.out.println("Crew group " + groupNumber + " schedule: ");
						System.out.print("Starting from ");
						for (int i1 = 0; i1 < tmax; i1++) {
							System.out.print("Day " + path[i1].routeDay + " from " + cityNames[path[i1].from] + " to " + cityNames[path[i1].to]);
							if (routesInfo[path[i1].routeDay][path[i1].from][path[i1].to].size()==0) {
								if (path[i1].from == path[i1].to) {
									System.out.println(" (rest);");
								}
								else {
									System.out.println(" (with other flight);");
								}
							}
							else {
								int route = routesInfo[path[i1].routeDay][path[i1].from][path[i1].to].get(0);
								System.out.println(" (route " + route + ");");
								routesInfo[path[i1].routeDay][path[i1].from][path[i1].to].remove(0);
							}
						}						
						opt--;
						System.out.println();
					}
					
				}*/
				
				
				
				
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