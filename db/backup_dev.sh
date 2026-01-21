dateStr=`date +%Y%m%d`
mysqldump --column-statistics=0 --opt --single-transaction -uroot -p"hF+6Kg-h+hTe" -h10.99.205.92 -P3306 regulation_qiqiao > regulation_qiqiao_$dateStr.sql