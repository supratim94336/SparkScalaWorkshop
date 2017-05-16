/**
  * Created by Supra on 28.04.17.
  */
package com.supra.scala
import org.apache.log4j._
import org.apache.spark.sql.SparkSession

object GermanLiga extends java.io.Serializable {

  // Here we are actually creating a collection/table structure: A user defined type
  case class Season(ssn:String, home:String, visitor:String, hgoal:Int, vgoal: Int, tier: String)

  // Mapping a line to a mapper function to produce each tuple as Person object
  def mapper(line: String): Season = {

    val fields = line.split(',')
    // variable person is assigned type Person and parameters for person is passed in brackets
    val season: Season = Season(fields(1),fields(2),fields(3),fields(5).toInt,fields(6).toInt,fields(7))

    return season
  }

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder
      .appName("SparkSQL")
      .master("local[*]")
      .getOrCreate()

    val lines = spark.sparkContext.textFile("../SparkScalaWorkshop/data/germany.csv")
    val header = lines.first()
    val data = lines.filter(row => row != header)
    val season = data.map(mapper)

    // Infer the schema and register the data-set as a table, it converts normal scala objects into data-frames
    import spark.implicits._

    val schemaSeason = season.toDS()

    schemaSeason.printSchema()

    // This line converts structured data to a table named people like a SQl table in a database
    schemaSeason.createOrReplaceTempView("season")

    val leaguetable = spark.sql("SELECT Team, Sum(P) AS P, Sum(W) AS W, Sum(D) AS D, Sum(L) AS L, SUM(F) as F," +
      "SUM(A) AS A,SUM(GD) AS GD, SUM(Pts) AS Pts FROM(SELECT home AS Team, 1 P, IF(hgoal > vgoal,1,0) W, " +
      "IF(hgoal = vgoal,1,0) D, IF(hgoal < vgoal,1,0) L, hgoal AS F, vgoal AS A, hgoal-vgoal AS GD, CASE WHEN " +
      "hgoal > vgoal THEN 3 WHEN hgoal = vgoal THEN 1 ELSE 0 END AS PTS FROM season WHERE ssn = 2015 UNION ALL " +
      "SELECT visitor, 1, " + "IF(hgoal < vgoal,1,0), IF(hgoal = vgoal,1,0), IF(hgoal > vgoal,1,0), vgoal, hgoal, " +
      "vgoal-hgoal GD," + "CASE WHEN hgoal < vgoal THEN 3 WHEN hgoal = vgoal THEN 1 ELSE 0 END FROM season WHERE ssn = "
      + "2015)" + "GROUP BY Team ORDER BY SUM(Pts) DESC, SUM(GD) DESC")

    // spark-sql results need to be collected
    val results = leaguetable.collect()

    results.foreach(println)

    spark.stop()
  }
}
