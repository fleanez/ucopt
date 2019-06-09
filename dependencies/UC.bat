rem set CLASSPATH=.\libs\
rem set PATH=.\libs
rem java -cp .;.\libs\;.\libs\mysql-connector-java-3.1.10-bin.jar;.\libs\glpk.jar;.\libs\lpsolve55j.jar UC
java -Djava.library.path=".\lib;.\lib\win64" -cp .;.\lib;.\lib\mysql-connector-java-3.1.10-bin.jar;.\lib\glpk.jar;.\lib\lpsolve55j.jar -jar UCOPT.jar
pause