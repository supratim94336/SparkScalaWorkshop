package com.supra.scala

import org.apache.spark.sql.SparkSession
import org.apache.log4j._
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.LinearRegression
/**
  * Created by Supra on 19/01/2017.
  */
object LinearRegressionDataFrame extends java.io.Serializable{

  def main(args: Array[String]): Unit ={

    // Setting Log level
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Creating spark context with app named "linear regression" using all the cores
    val spark = SparkSession.builder().appName("linear_regression").master("local[*]").getOrCreate()

    // Reading the regression data
    val input = spark.sparkContext.textFile("../SparkScalaWorkshop/data/regression.txt")

    // Dividing the dataset into labels and features as in machine learning we need
    val dataset = input.map(_.split(",")).map(x => (x(0).toDouble, Vectors.dense(x(1).toDouble)))

    // Starts the data framing part
    import spark.implicits._
    // The model works this way only where there is a label, there are features, and at last there are
    //  predictions
    val colnames = Seq("label", "features")
    val df = dataset.toDF(colnames: _*)

    // Dividing the trainset and testset as half of the whole dataset
    val trainTestDis = df.randomSplit(Array(0.5, 0.5))
    val trainSet = trainTestDis(0)
    val testSet = trainTestDis(1)

    // Creating Linear Regression model
    val lir = new LinearRegression()
                  .setRegParam(0.3)
                  .setElasticNetParam(0.8)
                  .setMaxIter(100)
                  .setTol(1E-6)

    // Prepare the model using training data and apply on test data
    val model = lir.fit(trainSet)
    val predictions = model.transform(testSet).cache()

    // This above step adds a prediction column - labels, features, prediction
    val predictionAndLabel = predictions.select("label","prediction").rdd.map(x => (x.getDouble(0),x.getDouble(1)))

    for(prediction <- predictionAndLabel) {
      println(prediction)
    }

    spark.stop()

  }

}
