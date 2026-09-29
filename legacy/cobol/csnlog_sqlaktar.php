<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\CSNLOG.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS CSNLOG (
        evrakno varchar(15),
        ctip int(2),
        sira int(10),
        tarih varchar(8),
        sonuc int(4),
        makbuzno varchar(15),
        tip int(2),
        optime numeric(16),        
        PRIMARY KEY (evrakno,ctip,sira)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update csnlog set evrakno = ?, ctip = ?, sira = ?, tarih = ?, sonuc = ?, makbuzno = ?, tip = ?, optime = ?
                            where evrakno = ? and ctip = ? and sira = ?";
$insert_query = "insert into csnlog values (?,?,?,?,?,?,?,?)";

$delete_query = "delete from csnlog where evrakno = ? and ctip = ? and sira = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("siisisidsii", $evrakno, $ctip, $sira, $tarih, $sonuc, $makbuzno, $tip, $optime, $evrakno, $ctip, $sira);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("siisisid", $evrakno, $ctip, $sira, $tarih, $sonuc, $makbuzno, $tip, $optime);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("sii", $evrakno, $ctip, $sira);

$dbex->query("START TRANSACTION");


$sql = 'select * from csnlog';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){

     $key = sprintf("%-15s%2s%10s", toCP857($row['evrakno']), 
                                    str_pad(number_format($row['ctip'],0,'',''),2,'0',STR_PAD_LEFT),
                                    str_pad(number_format($row['sira'],0,'',''),10,'0',STR_PAD_LEFT));
     $val = sprintf("%-15s%2s%10s%8s%4s%-15s%2s%16s",
                 toCP857($row['evrakno']),
                 str_pad(number_format($row['ctip'],0,'',''),2,'0',STR_PAD_LEFT),
                 str_pad(number_format($row['sira'],0,'',''),10,'0',STR_PAD_LEFT),
                 $row['tarih'], 
                 str_pad(number_format($row['sonuc'],0,'',''),4,'0',STR_PAD_LEFT),
                 toCP857($row['makbuzno']), 
                 str_pad(number_format($row['tip'],0,'',''),2,'0',STR_PAD_LEFT),
                 str_pad(number_format($row['optime'],0,'',''),16,'0',STR_PAD_LEFT)); 
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $evrakno = (string)  rtrim(toUTF8(substr($data[$key],0,15)));
           $ctip = floatVal(substr($data[$key],15,2));
           $sira = floatVal(substr($data[$key],17,10));
           $tarih = (string)  rtrim(toUTF8(substr($data[$key],27,8)));
           $sonuc = floatVal(substr($data[$key],35,4));
           $makbuzno = (string) rtrim(toUTF8(substr($data[$key],39,15)));
           $tip = floatVal(substr($data[$key],54,2));
           $optime = floatVal(substr($data[$key],56,16));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $ctip = $row['ctip']; $evrakno = $row['evrakno']; $sira = $row['sira'];
         $delete_stmt->execute();
     }
}

foreach($data as $key => $val) {
   $evrakno = (string)  rtrim(toUTF8(substr($data[$key],0,15)));
   $ctip = floatVal(substr($data[$key],15,2));
   $sira = floatVal(substr($data[$key],17,10));
   $tarih = (string)  rtrim(toUTF8(substr($data[$key],27,8)));
   $sonuc = floatVal(substr($data[$key],35,4));
   $makbuzno = (string) rtrim(toUTF8(substr($data[$key],39,15)));
   $tip = floatVal(substr($data[$key],54,2));
   $optime = floatVal(substr($data[$key],56,16));
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
