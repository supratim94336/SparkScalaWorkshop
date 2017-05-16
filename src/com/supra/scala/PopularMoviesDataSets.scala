package com.supra.scala

import org.apache.log4j._
import java.nio.charset.CodingErrorAction
import org.apache.spark.sql.SparkSession
import scala.io.Source
import scala.io.Codec
import org.apache.spark.sql.functions._

/**
  * Created by Supra on 15/01/2017.
  */
object PopularMoviesDataSets extends java.io.Serializable{

  def loadMovieNames(): Map[Int, String] = {
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    var movieNames: Map[Int, String] = Map()
    val lines = Source.fromFile("../SparkScalaWorkshop/data/ml-100k/u.item").getLines()
    for(line <- lines){
      val fields = line.split('|')
      if(fields.length > 1) {
        movieNames+= (fields(0).toInt -> fields(1))
      }
    }
    return movieNames
  }

  // Case class so that we can get a column name for our movieID
  // Each DS takes a class a data format
  final case class Movie(movieId: Int)

  // Our main function where action happens
  def main(args: Array[String]): Unit = {

    Logger.getLogger("org").setLevel(Level.ERROR)

    // SparkSession instead of SparkContext for Data-sets and Data-frames
    /**
      * SparkSession - New entry point of Spark
      * In earlier versions of spark, spark context was entry point for Spark. As RDD was main API, it was created and
      * manipulated using context API’s. For every other API,we needed to use different contexts. For streaming, we
      * needed StreamingContext, for SQL sqlContext and for hive HiveContext. But as DataSet and Dataframe API’s are
      * becoming new standard API’s we need an entry point build for them. So in Spark 2.0, we have a new entry point
      * for DataSet and Dataframe API’s called as Spark Session. SparkSession is essentially combination of SQLContext,
      * HiveContext and future StreamingContext. All the API’s available on those contexts are available on spark
      * session also. Spark session internally has a spark context for actual computation. So in rest of our post.
      */
    val spark = SparkSession
                .builder
                .appName("Pop_Movies")
                .master("local[*]")
                .getOrCreate()
    /*
     * SparkContext (aka Spark context) is the heart of a Spark application. You could also assume that a
     * SparkContext instance is a Spark application. Once a SparkContext is created you can use it to create RDDs,
     * accumulators and broadcast variables, access Spark services and run jobs (until SparkContext is stopped).
     */
    val lines = spark
                .sparkContext.textFile("../SparkScalaWorkshop/data/ml-100k/u.data")
                .map(x => Movie(x.split("\t")(1).toInt))

    // Convert to data set
    import spark.implicits._

    // At this point actually you have converted the data into an SQL Table
    val movieDS = lines.toDS()

    // Some SQL-style magic to sort all movies by popularity in one line! See the only column is named as movieID on the
    // run. count() creates a column named as count
    val topMovieIDS = movieDS
                      .groupBy("movieId")
                      .count()
                      .orderBy(desc("count"))
                      .cache()

    topMovieIDS.show()

    val top10 = topMovieIDS.take(10)

    val names = loadMovieNames()

    for(result <- top10){
      // Finding movie names from it's Integer ID and count part of result in data set is result(1)
      /**
        * Don't confuse the terms "cast" and "convert". The standard conversion methods begin with to, e.g. 20d.toInt
        * will convert a value 20 of type  Double a value 20 of type Int.
        * asInstanceOf on the other hand is a special type casting method. All it does is informs the compiler that the
        * value is of the type specified in its parameter, if during runtime the value on which you call this method
        * does not match with what you specified in the type parameter, you'll get an exception thrown.
        * I.e. in a.asInstanceOf[B] the provided value a must be of a type B or inherit from it - otherwise you'll get
        * an exception.

       */
      println(names(result(0).asInstanceOf[Int]) + ": " + result(1))
    }

    spark.stop()

  }
}
