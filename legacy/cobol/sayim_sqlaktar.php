<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\SAYIM.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}
$sql = "CREATE TABLE IF NOT EXISTS SAYIMDAT (
        yil int(4),
        katno int(4),
        stno varchar(15),
        sira int(10),
		depono int(4),
        prcno varchar(30),
		oemno varchar(30),
        tipi varchar(30),
        cinsi varchar(60),
        marka varchar(30),
		fiyat numeric (19,4),
		adet numeric (14,4),
        kdv numeric (9,4),
        raf varchar(15),
        PRIMARY KEY (yil, katno, stno, sira)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update sayimdat set yil = ?, katno = ?, stno = ?, sira=?, depono=?, prcno=?,  oemno=?, tipi=?,
                            cinsi = ?, marka = ?, fiyat = ?, adet = ?, kdv = ?, raf=? where yil = ? and katno = ? and stno = ? and sira = ?";
$insert_query = "insert into sayimdat values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

$delete_query = "delete from sayimdat where  yil = ? and katno = ? and stno = ? and sira = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("iisiisssssdddsiisi", $yil, $katno, $stno, $sira, $depono, $prcno, $oemno, $tipi, $cinsi, 
                                          $marka, $fiyat, $adet, $kdv, $raf, $yil, $katno, $stno, $sira);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("iisiisssssddds",  $yil, $katno, $stno, $sira, $depono, $prcno, $oemno, $tipi, $cinsi, 
                                          $marka, $fiyat, $adet, $kdv, $raf);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("iisi", $yil, $katno, $stno, $sira);

$dbex->query("START TRANSACTION");


$sql = 'select * from sayimdat';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
    
     $key = sprintf("%4s%4s%-15s%10s", 
				str_pad(number_format($row['yil'],0,'',''),4,'0',STR_PAD_LEFT), 
				str_pad(number_format($row['katno'],0,'',''),4,'0',STR_PAD_LEFT), 
				toCP857($row['stno']),
                str_pad(number_format($row['sira'],0,'',''),10,'0',STR_PAD_LEFT));
     $val = sprintf("%4s%4s%-15s%10s%4s%-30s%-30s%-30s%-60s%-30s%19s%14s%9s%-15s",
                str_pad(number_format($row['yil'],0,'',''),4,'0',STR_PAD_LEFT), 
				str_pad(number_format($row['katno'],0,'',''),4,'0',STR_PAD_LEFT), 
				toCP857($row['stno']),
                str_pad(number_format($row['sira'],0,'',''),10,'0',STR_PAD_LEFT),
				str_pad(number_format($row['depono'],0,'',''),4,'0',STR_PAD_LEFT),
				toCP857($row['prcno']), toCP857($row['oemno']), toCP857($row['tipi']),  toCP857($row['cinsi']), toCP857($row['marka']),
				str_pad(number_format($row['fiyat'],4,'',''), 19, '0', STR_PAD_LEFT),
				str_pad(number_format($row['adet'],4,'',''), 14, '0', STR_PAD_LEFT),
				str_pad(number_format($row['kdv'],4,'',''), 9, '0', STR_PAD_LEFT),
				toCP857($row['raf']));
				
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $yil = floatVal(substr($data[$key],0,4));
		   $katno = floatVal(substr($data[$key],4,4));
           $stno = (string)  rtrim(toUTF8(substr($data[$key],8,15)));
           $sira = floatVal(substr($data[$key],23,10));
           $depono = (string)  rtrim(toUTF8(substr($data[$key],33,4)));
           $prcno = (string) rtrim(toUTF8(substr($data[$key],37,30)));
           $oemno = (string)  rtrim(toUTF8(substr($data[$key],67,30)));
           $tipi = (string)  rtrim(toUTF8(substr($data[$key],97,30)));
           $cinsi =  (string) rtrim(toUTF8(substr($data[$key],127,60)));
		   $marka =  (string) rtrim(toUTF8(substr($data[$key],187,30)));
           $fiyat = floatVal(substr($data[$key],217,15) . '.' . substr($data[$key],232,4));
           $adet = floatVal(substr($data[$key],236,10) . '.' . substr($data[$key],246,4));
           $kdv = floatVal(substr($data[$key],250,5) . '.' . substr($data[$key],255,4));
           $raf = (string)  rtrim(toUTF8(substr($data[$key],259,15)));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $yil = $row['yil']; $katno = $row['katno']; $stno = $row['stno']; $sira = $row['sira'];
         $delete_stmt->execute();
     }
}

foreach($data as $key => $val) {
   $yil = floatVal(substr($data[$key],0,4));
   $katno = floatVal(substr($data[$key],4,4));
   $stno = (string)  rtrim(toUTF8(substr($data[$key],8,15)));
   $sira = floatVal(substr($data[$key],23,10));
   $depono = (string)  rtrim(toUTF8(substr($data[$key],33,4)));
   $prcno = (string) rtrim(toUTF8(substr($data[$key],37,30)));
   $oemno = (string)  rtrim(toUTF8(substr($data[$key],67,30)));
   $tipi = (string)  rtrim(toUTF8(substr($data[$key],97,30)));
   $cinsi =  (string) rtrim(toUTF8(substr($data[$key],127,60)));
   $marka =  (string) rtrim(toUTF8(substr($data[$key],187,30)));
   $fiyat = floatVal(substr($data[$key],217,15) . '.' . substr($data[$key],232,4));
   $adet = floatVal(substr($data[$key],236,10) . '.' . substr($data[$key],246,4));
   $kdv = floatVal(substr($data[$key],250,5) . '.' . substr($data[$key],255,4));
   $raf = (string)  rtrim(toUTF8(substr($data[$key],259,15)));
   $insert_stmt->execute();
   unset($data[$key]);           
   show_status($data_size - count($data), $data_size);
}
$update_stmt->close();
$insert_stmt->close();
$delete_stmt->close();
$dbex->query("COMMIT");
$dbex->close();
$db->close();


function toCP857($data) {
    return iconv('UTF-8','CP857', $data);
}
function toUTF8($data) {
    return iconv('CP857','UTF-8', $data);
}
?>
