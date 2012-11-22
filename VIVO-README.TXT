1. git svn remote (add to .git/config):

[svn-remote "svn"]
        url = https://javaxdelta.svn.sourceforge.net/svnroot/javaxdelta
        fetch = trunk/javaxdelta:refs/remotes/trunk

2. Change project version:

for x in $(grep -l -R 2.0.1.vivo_1 *); do sed -i -e 's/2.0.1.vivo_1/2.0.1.vivo_1.vivo_1/g' $x; done;
