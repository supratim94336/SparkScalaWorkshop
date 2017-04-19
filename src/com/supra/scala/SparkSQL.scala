package com.supra.scala
import org.apache.log4j._
import org.apache.spark.sql.SparkSession


/**
  * Created by Supra on 15/01/2017.
  */
object SparkSQL extends java.io.Serializable {

  // Here we are actually creating a collection/table structure: A user defined type
  case class Person(ID:Int, name:String, age:Int, numFriends:Int)

  // Mapping a line to a mapper function to produce each tuple as Person object
  def mapper(line: String): Person = {

    val fields = line.split(',')
    // variable person is assigned type Person and parameters for person is passed in brackets
    val person: Person = Person(fields(0).toInt,fields(1),fields(2).toInt,fields(3).toInt)

    return person
  }

  /**
    * Here you can actually understand that a sparkSession object contains
    * both sql and sparkContext
    * The Dataset is actually used to create a schema of name people, which we can query
    * and collect results of
    */
  def main(args: Array[String]): Unit = {

    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
                .builder
                .appName("SparkSQL")
                .master("local[*]")
                .getOrCreate()

    val lines = spark.sparkContext.textFile("../SparkScalaWorkshop/data/fakefriends.csv")
    val people = lines.map(mapper)

    // Infer the schema and register the data-set as a table, it converts normal scala objects into data-frames
    import spark.implicits._

    val schemaPeople = people.toDS()

    schemaPeople.printSchema()

    // This line converts structured data to a table named people
    schemaPeople.createOrReplaceTempView("people")

    val teenagers = spark.sql("SELECT * FROM people WHERE age>=13 AND age<=19")

    // spark-sql results need to be collected
    val results = teenagers.collect()

    results.foreach(println)

    spark.stop()
  }
}
