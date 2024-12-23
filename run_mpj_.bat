javac -d out -cp "E:\paralelRGR\rgr\mpj-v0_44\mpj-v0_44\lib\*" E:\paralelRGR\rgr\src\main\java\org\example\*.java
mpjrun.bat -np 8 -cp "out;E:\paralelRGR\rgr\mpj-v0_44\mpj-v0_44\lib\*" org.example.MainTest E:\paralelRGR\rgr\mpj-v0_44\mpj-v0_44\conf\xdev.conf Local


