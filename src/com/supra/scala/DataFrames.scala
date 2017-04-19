package com.supra.scala
import org.apache.log4j._
import org.apache.spark.sql.SparkSession
/**
  * Created by Supra on 15/01/2017.
  */
object DataFrames extends java.io.Serializable{

  case class Person(ID:Int, name:String, age:Int, numFriends:Int)

  def mapper(line:String): Person = {
    val fields = line.split(',')
    val person: Person = Person(fields(0).toInt, fields(1), fields(2).toInt, fields(3).toInt)
    return person
  }

  def main(args: Array[String]): Unit = {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Use new SparkSession interface in Spark 2.0
    val spark = SparkSession
      .builder
      .appName("SparkSQL")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._
    val lines = spark.sparkContext.textFile("../SparkScalaWorkshop/data/fakefriends.csv")
    val people = lines.map(mapper).toDS().cache()

    // Other ways to read a table
    // spark.read.json("json file path")
    // sqlContext.table("Hive table name")

    println("The inferred schema: ")
    people.printSchema()

    println("Lets select the name column: ")
    people.select("name").show()

    // Filtering out ages less than 21
    println("Filtering out people below 21: ")
    people.filter(people("age") < 21).show()

    // Grouping by age
    println("Group by age: ")
    people.groupBy("age").count.show()

    // Making everyone 10 years older
    println("Make everyone 10 years older: ")
    people.select(people("name"), people("age") + 10).show()

    spark.stop()
  }

}

