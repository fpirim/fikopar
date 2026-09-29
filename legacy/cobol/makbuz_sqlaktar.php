<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\MAKBUZ.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}
           
$sql = "CREATE TABLE IF NOT EXISTS MAKBUZ (
        evrakno varchar(15),
        tip int(2),
        sekilno int(4),
        hesapno varchar(15),
        depono int(4),
        tarih varchar(8),
        aciklama varchar(60),
        tutar numeric(15,2),
        PRIMARY KEY (evrakno,tip),
        INDEX (hesapno)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update makbuz set evrakno=?, tip=?, sekilno = ?, hesapno = ?, depono=?, tarih=?,  aciklama=?,
                            tutar = ? where evrakno = ? and tip = ?";
$insert_query = "insert into makbuz values (?,?,?,?,?,?,?,?)";

$delete_query = "delete from makbuz where evrakno = ? and tip = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("siisissdsi", $evrakno, $tip, $sekilno, $hesapno, $depono, $tarih, $aciklama, $tutar, $evrakno, $tip);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("siisissd", $evrakno, $tip, $sekilno, $hesapno, $depono, $tarih, $aciklama, $tutar);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("si", $evrakno, $tip);

$dbex->query("START TRANSACTION");


$sql = 'select * from makbuz';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

while($row = $result->fetch_assoc()){
     $key = sprintf("%-15s%2s", toCP857($row['evrakno']), str_pad(number_format($row['tip'],0,'',''),2,'0',STR_PAD_LEFT));
     $val = sprintf("%-15s%2s%4s%-15s%4s%8s%-60s%-15s",  toCP857($row['evrakno']),
                 str_pad(number_format($row['tip'],0,'',''),2,'0',STR_PAD_LEFT), 
                 str_pad(number_format($row['sekilno'],0,'',''),4,'0',STR_PAD_LEFT),
                 toCP857($row['hesapno']),
                 str_pad(number_format($row['depono'],0,'',''),4,'0',STR_PAD_LEFT),
                 $row['tarih'],
                 toCP857($row['aciklama']),
                 str_pad(number_format($row['tutar'],2,'',''), 15, '0', STR_PAD_LEFT)); 
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $evrakno = (string) rtrim(toUTF8(substr($data[$key],0,15)));
           $tip = floatVal(substr($data[$key],15,2));
           $sekilno = floatVal(substr($data[$key],17,4));
           $hesapno = (string) rtrim(toUTF8(substr($data[$key],21,15)));
           $depono = floatVal(substr($data[$key],36,4));
           $tarih = (string) substr($data[$key],40,8);
           $aciklama  = (string) rtrim(toUTF8(substr($data[$key],48,60)));
           $tutar = floatVal(substr($data[$key],108,13) . '.' . substr($data[$key],121,2));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $evrakno = $row['evrakno'];
         $tip = $row['tip'];
         $delete_stmt->execute();
     }
}

foreach($data as $key => $val) {
   $evrakno = (string) rtrim(toUTF8(substr($data[$key],0,15)));
   $tip = floatVal(substr($data[$key],15,2));
   $sekilno = floatVal(substr($data[$key],17,4));
   $hesapno = (string) rtrim(toUTF8(substr($data[$key],21,15)));
   $depono = floatVal(substr($data[$key],36,4));
   $tarih = (string) substr($data[$key],40,8);
   $aciklama  = (string) rtrim(toUTF8(substr($data[$key],48,60)));
   $tutar = floatVal(substr($data[$key],108,13) . '.' . substr($data[$key],121,2));
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
