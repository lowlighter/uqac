//Imports
  import org.apache.spark.sql.SparkSession

//Source
  object Exercise1 extends App {

    //Apache Spark
      val spark = SparkSession.builder
        .master("local")
        .appName("Exercise 1")
        .getOrCreate()
      spark.sparkContext.setLogLevel("ERROR")

    //Retrieve data from JSON file
      val spells = spark.sqlContext.read
        .option("multiLine", true)
        .json("src/main/resources/spells.json")

    //Method 1 : Through filters and RDD
      println(s"Peto may choose one of the following solely verbal spells found using the Filter-Map-Reduce Encyclopedia to get out alive :")
      spells.rdd
        .filter(row => (row.getAs[Any]("level") != null) && (row.getAs[Long]("level") <= 4) && row.getAs[Seq[String]]("components").contains("V") && (row.getAs[Seq[String]]("components").length == 1) )
        .map(row => f"- ${row.getAs[String]("name")} (level ${row.getAs[Long]("level")})")
        .take(30)
        .foreach(println)

    //Method 2 : Through SQL query and DataFrame
      println(s"\nPeto may choose one of the following solely verbal spells found using the SQL Encyclopedia to get out alive :")
      spells
        .cache()
        .createOrReplaceTempView("spells")
      spark.sqlContext
        .sql("SELECT name, level FROM spells WHERE (level IS NOT NULL) AND (level <= 4) AND (ARRAY_CONTAINS(components, 'V')) AND (CARDINALITY(components) == 1)")
        .show(30)

  }
