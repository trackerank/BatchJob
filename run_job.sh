CURRENT_DATE=`date '+%Y/%m/%d'`
LESSON=$(basename $PWD)
mvn clean package -Dmaven.test.skip=true;
java -jar ./target/BatchJob-1.jar "run.date(date)=$CURRENT_DATE";
read;
