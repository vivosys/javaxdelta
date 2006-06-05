#! /bin/sh
if  [ "x$3" != "x" -a -x $2 ]; then
    java -cp ./classes: com.nothome.delta.GDiffPatcher $1 $2 $3;
else
   echo "useage: java -cp ./classes com.nothome.delta.GDiffPatcher source delta result";
fi
