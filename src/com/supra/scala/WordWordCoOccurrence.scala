package com.supra.scala

import java.nio.charset.CodingErrorAction
import java.util

import scala.collection.JavaConversions._
import com.google.common.collect.{HashBasedTable, Table}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.api.java.JavaRDD

import scala.io.{Codec, Source}

/**
  * Created by Supra on 05/01/2017.
  */
object WordWordCoOccurrence extends java.io.Serializable {
  def coOccurrence(line: String) : Array[String] = {
    var fields = line.split(" ")
    var stopwords: Set[String] = Set()
    var wordword : Array[String] = new Array[String](100000)
    val stopis = Source.fromFile("../SparkScalaWorkshop/data/stop_words.txt").getLines.toList
    for(stop <- stopis){
      fields.filter(_==stop)
    }

    /*val token_list = util.Arrays.asList(fields)
    val utoken_List = new util.HashSet(token_list)
    val utoken_List_array = utoken_List.toArray(new Array[String](utoken_List.size))
    //Creating list of token-token-frequency list
    */
    var j = 0
    var l = 0
    while(j < fields.length){
      var k = j+1
      while(k<fields.length){
        var word1 = fields(j).toLowerCase().replace(",","").replace(".","").replace("\"","").replace("!","")
          .replace("?","").replace(";","").replace("%","").replace(";","").replace(")","").replace("(","").replace(":","")
        var word2 = fields(k).toLowerCase()
          .replace(",","").replace(".","").replace("\"","").replace("!","").replace("?","").replace("%","").replace(";","")
          .replace(")","").replace("(","").replace(":","")
        if(word1!="" && word2!="" && word1!=word2)
        wordword(l) = word1 + "\t" + word2
        l+=1
        k+=1
      }
      j+=1
    }
    return wordword
  }

  def main(args: Array[String]) {
    var uniqueSet: Table[String,String,Int] = HashBasedTable.create()
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)
    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "WordWordCounter")
    // Read each line of input data
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    val wholetext = sc.textFile("/Users/Supra/Documents/workspace/loadConstruction/localOutputs/Runner/97/AFP_ENG_19960428.0410.txt")

    // Separate by stop and space
    //val lines = wholetext.flatMap(x=>x.split(".+"))

    // Creating pairs of words from sentences
    val pairs = wholetext.flatMap(l => l.split("\n")).flatMap(l => l.split(" ").toList.combinations(2))

    // Creating key-value pairs of pair-of-words
    val pairCount = pairs.map(l => (l,1))

    val countResults = pairCount.reduceByKey((x,y) => x+y)
    //println(line)

    // Convert to (stationID, temperature)
    //val counts = words.map(x => (x,1))

    // Finally the count of individual words
    //val countResults = line.reduceByKey(_+_)
    //val lineCounts = line.reduceByKey(_+_)
    // Flip the count results value as key, key as value
    //val countResultsRefined = countResults.sortByKey()

    // Collect, format, and print the results
    val results = countResults.collect()
    //val results = lineCounts.collect()

    for (result <- results) {
      if(result!=null) {
        //var words = result._1.split("\t")
        var word1 = result._1.get(0)
        var word2 = result._1.get(1)
        //var count = result._2.toInt
        //println(result._2.getClass)
        if (!uniqueSet.contains(word1, word2)) {
          if (!uniqueSet.contains(word2, word1)) {
            if(word1!=word2){
              uniqueSet.put(word1, word2, result._2)
            }
          }
          var tempCount = uniqueSet.get(word2, word1)
          uniqueSet.remove(word2, word1)
          if(word1!=word2){
            uniqueSet.put(word1, word2, tempCount)
          }
        }
      }
    }
    for (cell <- uniqueSet.cellSet) {
      println(cell.getRowKey() + "\t" + cell.getColumnKey() + "\t" + cell.getValue());
    }

    /*
    for(result <- results){
      println(result._1.toString() + "-->" + result._2)
    }
    */
  }

}
