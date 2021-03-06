/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.Library;

import com.mikroskil.elm_skripsi.model.Record;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;

/**
 *
 * @author Andy Wijaya
 */
public class LearningMachine {
    private DenseMatrix train_set;
	private DenseMatrix test_set;
	private int numTrainData;
	private int numTestData;
	private DenseMatrix InputWeight;
	private float TrainingTime;
	private float TestingTime;
	private double TrainingAccuracy, TestingAccuracy;
	private int Elm_Type;
	private int NumberofHiddenNeurons;
	private int NumberofOutputNeurons;						//also the number of classes
	private int NumberofInputNeurons;						//also the number of attribution
	private String func;
	private int []label;
	//this class label employ a lazy and easy method,any class must written in 0,1,2...so the preprocessing is required

	//the blow variables in both train() and test()
	private DenseMatrix  BiasofHiddenNeurons;
	private DenseMatrix  OutputWeight;
	private DenseMatrix  testP;
	private DenseMatrix  testT;
	private DenseMatrix  Y;
	private DenseMatrix  T;
        
        private DenseMatrix firstInputWeight, firstBiasofHiddenNeurons;
        
        private int seed;
        private Random rand;
        /**
     * Construct an ELM
     * @param
     * elm_type              - 0 for regression; 1 for (both binary and multi-classes) classification
     * @param
     * numberofHiddenNeurons - Number of hidden neurons assigned to the ELM
     * @param
     * ActivationFunction    - Type of activation function:
     *                      'sig' for Sigmoidal function
     *                      'sin' for Sine function
     *                      'hardlim' for Hardlim function
     *                      'tribas' for Triangular basis function
     *                      'radbas' for Radial basis function (for additive type of SLFNs instead of RBF type of SLFNs)
     * @throws NotConvergedException
     */

	public LearningMachine(int elm_type, int seed, String ActivationFunction){



		Elm_Type = elm_type;
		NumberofHiddenNeurons = 20;
		func = ActivationFunction;

		TrainingTime = 0;
		TestingTime = 0;
		TrainingAccuracy= 0;
		TestingAccuracy = 0;
		NumberofOutputNeurons = 1;
                
                this.seed = seed;
                        
                rand = new Random(seed);
                
	}
        
	public LearningMachine(){}
	//the first line of dataset file must be the number of rows and columns,and number of classes if neccessary
	//the first column is the norminal class value 0,1,2...
	//if the class value is 1,2...,number of classes should plus 1
        /** Perbaiki **/
	public DenseMatrix loadmatrix(String filename) throws IOException{

		BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
		//FileInputStream
		String firstlineString = reader.readLine();
		String []strings = firstlineString.split(" ");
		int m = Integer.parseInt(strings[0]);
		int n = Integer.parseInt(strings[1]);
		if(strings.length > 2)
			NumberofOutputNeurons = Integer.parseInt(strings[2]);


		DenseMatrix matrix = new DenseMatrix(m, n);

		firstlineString = reader.readLine();
		int i = 0;
		while (i<m) {
			String []datatrings = firstlineString.split(" ");
			for (int j = 0; j < n; j++) {
                            matrix.set(i, j, Double.parseDouble(datatrings[j]));
			}
			i++;
			firstlineString = reader.readLine();
		}
		/*
		for(int ii = 0; ii<m; ii++)
			matrix.add(ii, 0, -1);
		*/
		return matrix;
	}
        
        public DenseMatrix loadMatrix(ArrayList<Record> records){
            DenseMatrix matrix = new DenseMatrix(records.size(), 7);
            for(int i=0;i<records.size();i++){
                String[] stringLines = records.get(i).toArrayWithoutDate();
                for(int j=0;j<stringLines.length;j++){
                    matrix.set(i,j,Double.parseDouble(stringLines[j]));
                }
            }
            return matrix;
        }

        public void train(ArrayList<Record> records) throws NotConvergedException{
            train_set = loadMatrix(records);
            train();
        }
        
        /** Sudah Oke*/
	private void train() throws NotConvergedException{

		numTrainData = train_set.numRows();
		NumberofInputNeurons = train_set.numColumns()-1;
		InputWeight = fetchInputWeight();

		DenseMatrix transT = new DenseMatrix(numTrainData, 1);
		DenseMatrix transP = new DenseMatrix(numTrainData, NumberofInputNeurons);
		for (int i = 0; i < numTrainData; i++) {
			transT.set(i, 0, train_set.get(i, 0));
			for (int j = 1; j <= NumberofInputNeurons; j++)
				transP.set(i, j-1
                                        , train_set.get(i, j));
		}
		T = new DenseMatrix(1,numTrainData);
		DenseMatrix P = new DenseMatrix(NumberofInputNeurons,numTrainData);
		transT.transpose(T);
		transP.transpose(P);
                
		if(Elm_Type != 0)	//CLASSIFIER
		{
			label = new int[NumberofOutputNeurons];
			for (int i = 0; i < NumberofOutputNeurons; i++) {
				label[i] = i;							//class label starts form 0
			}
			DenseMatrix tempT = new DenseMatrix(NumberofOutputNeurons,numTrainData);
			tempT.zero();
			for (int i = 0; i < numTrainData; i++){
					int j = 0;
			        for (j = 0; j < NumberofOutputNeurons; j++){
			            if (label[j] == T.get(0, i))
			                break;
			        }
			        tempT.set(j, i, 1);
			}

			T = new DenseMatrix(NumberofOutputNeurons,numTrainData);	// T=temp_T*2-1;
			for (int i = 0; i < NumberofOutputNeurons; i++){
		        for (int j = 0; j < numTrainData; j++)
		        	T.set(i, j, tempT.get(i, j)*2-1);
			}

			transT = new DenseMatrix(numTrainData,NumberofOutputNeurons);
			T.transpose(transT);

		} 	//end if CLASSIFIER

		long start_time_train = System.currentTimeMillis();
		// Random generate input weights InputWeight (w_i) and biases BiasofHiddenNeurons (b_i) of hidden neurons
		// InputWeight=rand(NumberofHiddenNeurons,NumberofInputNeurons)*2-1;

		BiasofHiddenNeurons = fetchBiasofHiddenNeurons();

		DenseMatrix tempH = new DenseMatrix(NumberofHiddenNeurons, numTrainData);
		InputWeight.mult(P, tempH);
                
		//DenseMatrix ind = new DenseMatrix(1, numTrainData);

		DenseMatrix BiasMatrix = new DenseMatrix(NumberofHiddenNeurons, numTrainData);

		for (int j = 0; j < numTrainData; j++) {
			for (int i = 0; i < NumberofHiddenNeurons; i++) {
				BiasMatrix.set(i, j, BiasofHiddenNeurons.get(i, 0));
			}
		}

		tempH.add(BiasMatrix);
		DenseMatrix H = new DenseMatrix(NumberofHiddenNeurons, numTrainData);

		if(func.startsWith("sig")){
			for (int j = 0; j < NumberofHiddenNeurons; j++) {
				for (int i = 0; i < numTrainData; i++) {
					double temp = tempH.get(j, i);
					temp = 1.0f/ (1 + Math.exp(-temp));
					H.set(j, i, temp);
				}
			}
		}
		else if(func.startsWith("sin")){
			for (int j = 0; j < NumberofHiddenNeurons; j++) {
				for (int i = 0; i < numTrainData; i++) {
					double temp = tempH.get(j, i);
					temp = Math.sin(temp);
					H.set(j, i, temp);
				}
			}
		}
		else if(func.startsWith("hardlim")){
			//If you need it ,you can absolutely complete it yourself
		}
		else if(func.startsWith("tribas")){
			//If you need it ,you can absolutely complete it yourself
		}
		else if(func.startsWith("radbas")){
			//If you need it ,you can absolutely complete it yourself
		}

		DenseMatrix Ht = new DenseMatrix(numTrainData,NumberofHiddenNeurons);
		H.transpose(Ht);
		Inverse invers = new Inverse(Ht);
		DenseMatrix pinvHt = invers.getMPInverse();			//NumberofHiddenNeurons*numTrainData
		//DenseMatrix pinvHt = invers.getMPInverse(0.000001); //fast method, PLEASE CITE in your paper properly:
		//Guang-Bin Huang, Hongming Zhou, Xiaojian Ding, and Rui Zhang, "Extreme Learning Machine for Regression and Multi-Class Classification," submitted to IEEE Transactions on Pattern Analysis and Machine Intelligence, October 2010.

		OutputWeight = new DenseMatrix(NumberofHiddenNeurons, NumberofOutputNeurons);
		//OutputWeight=pinv(H') * T';
		pinvHt.mult(transT, OutputWeight);

		long end_time_train = System.currentTimeMillis();
		TrainingTime = (end_time_train - start_time_train)*1.0f/1000;

		DenseMatrix Yt = new DenseMatrix(numTrainData,NumberofOutputNeurons);
		Ht.mult(OutputWeight,Yt);
		Y = new DenseMatrix(NumberofOutputNeurons,numTrainData);
		Yt.transpose(Y);

		if(Elm_Type == 0){
			double MSE = 0;
			for (int i = 0; i < numTrainData; i++) {
				MSE += (Yt.get(i, 0) - transT.get(i, 0))*(Yt.get(i, 0) - transT.get(i, 0));
			}
			TrainingAccuracy = Math.sqrt(MSE/numTrainData);
		}

		//CLASSIFIER
		else if(Elm_Type == 1){
			float MissClassificationRate_Training=0;

		    for (int i = 0; i < numTrainData; i++) {
				double maxtag1 = Y.get(0, i);
				int tag1 = 0;
				double maxtag2 = T.get(0, i);
				int tag2 = 0;
		    	for (int j = 1; j < NumberofOutputNeurons; j++) {
					if(Y.get(j, i) > maxtag1){
						maxtag1 = Y.get(j, i);
						tag1 = j;
					}
					if(T.get(j, i) > maxtag2){
						maxtag2 = T.get(j, i);
						tag2 = j;
					}
				}
		    	if(tag1 != tag2)
		    		MissClassificationRate_Training ++;
			}
		    TrainingAccuracy = 1 - MissClassificationRate_Training*1.0f/numTrainData;

		}

	}
        
        public void test(ArrayList<Record> records){
            test_set = loadMatrix(records);
            test();
        }
        
	private void test(){
		numTestData = test_set.numRows();
		DenseMatrix ttestT = new DenseMatrix(numTestData, 1);
		DenseMatrix ttestP = new DenseMatrix(numTestData, NumberofInputNeurons);
		for (int i = 0; i < numTestData; i++) {
			ttestT.set(i, 0, test_set.get(i, 0));
			for (int j = 1; j <= NumberofInputNeurons; j++)
				ttestP.set(i, j-1, test_set.get(i, j));
		}

		testT = new DenseMatrix(1,numTestData);
		testP = new DenseMatrix(NumberofInputNeurons,numTestData);
		ttestT.transpose(testT);
		ttestP.transpose(testP);

		long start_time_test = System.currentTimeMillis();
		DenseMatrix tempH_test = new DenseMatrix(NumberofHiddenNeurons, numTestData);
		InputWeight.mult(testP, tempH_test);
		DenseMatrix BiasMatrix2 = new DenseMatrix(NumberofHiddenNeurons, numTestData);

		for (int j = 0; j < numTestData; j++) {
			for (int i = 0; i < NumberofHiddenNeurons; i++) {
				BiasMatrix2.set(i, j, BiasofHiddenNeurons.get(i, 0));
			}
		}

		tempH_test.add(BiasMatrix2);
		DenseMatrix H_test = new DenseMatrix(NumberofHiddenNeurons, numTestData);

		if(func.startsWith("sig")){
			for (int j = 0; j < NumberofHiddenNeurons; j++) {
				for (int i = 0; i < numTestData; i++) {
					double temp = tempH_test.get(j, i);
					temp = 1.0f/ (1 + Math.exp(-temp));
					H_test.set(j, i, temp);
				}
			}
		}
		else if(func.startsWith("sin")){
			for (int j = 0; j < NumberofHiddenNeurons; j++) {
				for (int i = 0; i < numTestData; i++) {
					double temp = tempH_test.get(j, i);
					temp = Math.sin(temp);
					H_test.set(j, i, temp);
				}
			}
		}
		else if(func.startsWith("hardlim")){

		}
		else if(func.startsWith("tribas")){

		}
		else if(func.startsWith("radbas")){

		}

		DenseMatrix transH_test = new DenseMatrix(numTestData,NumberofHiddenNeurons);
		H_test.transpose(transH_test);
		DenseMatrix Yout = new DenseMatrix(numTestData,NumberofOutputNeurons);
		transH_test.mult(OutputWeight,Yout);

		DenseMatrix testY = new DenseMatrix(NumberofOutputNeurons,numTestData);
		Yout.transpose(testY);

		long end_time_test = System.currentTimeMillis();
		TestingTime = (end_time_test - start_time_test)*1.0f/1000;

		//REGRESSION
		if(Elm_Type == 0){
			double MSE = 0;
			for (int i = 0; i < numTestData; i++) {
				MSE += (Yout.get(i, 0) - testT.get(0,i))*(Yout.get(i, 0) - testT.get(0,i));
			}
			TestingAccuracy = Math.sqrt(MSE/numTestData);
		}


		//CLASSIFIER
		else if(Elm_Type == 1){

			DenseMatrix temptestT = new DenseMatrix(NumberofOutputNeurons,numTestData);
			for (int i = 0; i < numTestData; i++){
					int j = 0;
			        for (j = 0; j < NumberofOutputNeurons; j++){
			            if (label[j] == testT.get(0, i))
			                break;
			        }
			        temptestT.set(j, i, 1);
			}

			testT = new DenseMatrix(NumberofOutputNeurons,numTestData);
			for (int i = 0; i < NumberofOutputNeurons; i++){
		        for (int j = 0; j < numTestData; j++)
		        	testT.set(i, j, temptestT.get(i, j)*2-1);
			}

		    float MissClassificationRate_Testing=0;

		    for (int i = 0; i < numTestData; i++) {
				double maxtag1 = testY.get(0, i);
				int tag1 = 0;
				double maxtag2 = testT.get(0, i);
				int tag2 = 0;
		    	for (int j = 1; j < NumberofOutputNeurons; j++) {
					if(testY.get(j, i) > maxtag1){
						maxtag1 = testY.get(j, i);
						tag1 = j;
					}
					if(testT.get(j, i) > maxtag2){
						maxtag2 = testT.get(j, i);
						tag2 = j;
					}
				}
		    	if(tag1 != tag2)
		    		MissClassificationRate_Testing ++;
			}
		    TestingAccuracy = 1 - MissClassificationRate_Testing*1.0f/numTestData;

		}
	}

        public double[] testOut(ArrayList<Record> records){
            test_set = loadMatrix(records);
            return testOut();
        }
        
        public double[] testOut(double[][] records){
            test_set = new DenseMatrix(records);
            return testOut();
        }
        
	//Output	numTestData*NumberofOutputNeurons
	private double[] testOut(){
		numTestData = test_set.numRows();
		NumberofInputNeurons = test_set.numColumns()-1;

		DenseMatrix ttestT = new DenseMatrix(numTestData, 1);
		DenseMatrix ttestP = new DenseMatrix(numTestData, NumberofInputNeurons);
		for (int i = 0; i < numTestData; i++) {
			ttestT.set(i, 0, test_set.get(i, 0));
			for (int j = 1; j <= NumberofInputNeurons; j++)
				ttestP.set(i, j-1, test_set.get(i, j));
		}

		testT = new DenseMatrix(1,numTestData);
		testP = new DenseMatrix(NumberofInputNeurons,numTestData);
		ttestT.transpose(testT);
		ttestP.transpose(testP);
		//test_set.transpose(testP);
                
		long start_time_test = System.currentTimeMillis();
		DenseMatrix tempH_test = new DenseMatrix(NumberofHiddenNeurons, numTestData);
		InputWeight.mult(testP, tempH_test);
		DenseMatrix BiasMatrix2 = new DenseMatrix(NumberofHiddenNeurons, numTestData);

		for (int j = 0; j < numTestData; j++) {
			for (int i = 0; i < NumberofHiddenNeurons; i++) {
				BiasMatrix2.set(i, j, BiasofHiddenNeurons.get(i, 0));
			}
		}

		tempH_test.add(BiasMatrix2);
		DenseMatrix H_test = new DenseMatrix(NumberofHiddenNeurons, numTestData);

		if(func.startsWith("sig")){
			for (int j = 0; j < NumberofHiddenNeurons; j++) {
				for (int i = 0; i < numTestData; i++) {
					double temp = tempH_test.get(j, i);
					temp = 1.0f/ (1 + Math.exp(-temp));
					H_test.set(j, i, temp);
				}
			}
		}
		else if(func.startsWith("sin")){
			for (int j = 0; j < NumberofHiddenNeurons; j++) {
				for (int i = 0; i < numTestData; i++) {
					double temp = tempH_test.get(j, i);
					temp = Math.sin(temp);
					H_test.set(j, i, temp);
				}
			}
		}
		else if(func.startsWith("hardlim")){

		}
		else if(func.startsWith("tribas")){

		}
		else if(func.startsWith("radbas")){

		}

		DenseMatrix transH_test = new DenseMatrix(numTestData,NumberofHiddenNeurons);
		H_test.transpose(transH_test);
		DenseMatrix Yout = new DenseMatrix(numTestData,NumberofOutputNeurons);
		transH_test.mult(OutputWeight,Yout);
                
                long end_time_test = System.currentTimeMillis();
		TestingTime = (end_time_test - start_time_test)*1.0f/1000;
		//DenseMatrix testY = new DenseMatrix(NumberofOutputNeurons,numTestData);
		//Yout.transpose(testY);

		double[] result = new double[numTestData];

		if(Elm_Type == 0){
			for (int i = 0; i < numTestData; i++)
				result[i] = Yout.get(i, 0);
		}

		else if(Elm_Type == 1){
			for (int i = 0; i < numTestData; i++) {
				int tagmax = 0;
				double tagvalue = Yout.get(i, 0);
				for (int j = 1; j < NumberofOutputNeurons; j++)
				{
					if(Yout.get(i, j) > tagvalue){
						tagvalue = Yout.get(i, j);
						tagmax = j;
					}

				}
				result[i] = tagmax;
			}
		}
		return result;
	}

	public float getTrainingTime() {
		return TrainingTime;
	}
	public double getTrainingAccuracy() {
		return TrainingAccuracy;
	}
	public float getTestingTime() {
		return TestingTime;
	}
	public double getTestingAccuracy() {
		return TestingAccuracy;
	}

	public int getNumberofInputNeurons() {
		return NumberofInputNeurons;
	}
	public int getNumberofHiddenNeurons() {
		return NumberofHiddenNeurons;
	}
	public int getNumberofOutputNeurons() {
		return NumberofOutputNeurons;
	}

	public DenseMatrix getInputWeight() {
		return InputWeight;
	}

	public DenseMatrix getBiasofHiddenNeurons() {
		return BiasofHiddenNeurons;
	}

	public DenseMatrix getOutputWeight() {
		return OutputWeight;
	}
        
        private DenseMatrix fetchInputWeight(){
            double[][] tempIW = new double[NumberofHiddenNeurons][NumberofInputNeurons];
            for(int i=0;i<NumberofHiddenNeurons;i++){
                for(int j=0;j<NumberofInputNeurons;j++){
                    tempIW[i][j] = rand.nextDouble();
                }
            }
            return new DenseMatrix(tempIW);
        }
        
        private DenseMatrix fetchBiasofHiddenNeurons(){
            double[][] tempBHN = new double[NumberofHiddenNeurons][1];
            for(int i=0;i<NumberofHiddenNeurons;i++){
                for(int j=0;j<1;j++){
                    tempBHN[i][j]=rand.nextDouble();
                }
            }
            return new DenseMatrix(tempBHN);
        }

        public DenseMatrix getFirstInputWeight() {
            return firstInputWeight;
        }

        public DenseMatrix getFirstBiasofHiddenNeurons() {
            return firstBiasofHiddenNeurons;
        }

        public int getSeed() {
            return seed;
        }
        
	//for predicting a data file based on a trained model.
	public void testgetoutput(String filename) throws IOException {

		try {
			test_set = loadmatrix(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		numTestData = test_set.numRows();
		NumberofInputNeurons = test_set.numColumns() - 1;


		double rsum = 0;
		double []actual = new double[numTestData];

		double [][]data = new double[numTestData][NumberofInputNeurons];
		for (int i = 0; i < numTestData; i++) {
			actual[i] = test_set.get(i, 0);
			for (int j = 0; j < NumberofInputNeurons; j++)
				data[i][j] = test_set.get(i, j+1);
		}

		double[] output = testOut(data);
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("Output")));
		for (int i = 0; i < numTestData; i++) {

			writer.write(String.valueOf(output[i]));
			writer.newLine();

			if(Elm_Type == 0){
					rsum += (output[i] - actual[i])*(output[i] - actual[i]);
			}

			if(Elm_Type == 1){
				if(output[i] == actual[i])
					rsum ++;
			}

		}
		writer.flush();
		writer.close();

		if(Elm_Type == 0)
			System.out.println("Regression GetOutPut RMSE: "+Math.sqrt(rsum*1.0f/numTestData));
		else if(Elm_Type == 1)
			System.out.println("Classfy GetOutPut Right: "+rsum*1.0f/numTestData);
	}
}
