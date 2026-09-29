<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\KASA.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}
       
$sql = "CREATE TABLE IF NOT EXISTS KASA (
        tarih varchar(8),
        sira int(5),
        hesapno varchar(15),
        aciklama varchar(60),
        gelir numeric(15,2),
        gider numeric(15,2),
        tip int(2),
        evrakno varchar(15),
        PRIMARY KEY (tarih,sira)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update kasa set tarih = ?, sira = ?, hesapno = ?, aciklama = ?, gelir = ?, gider = ?, tip = ?, evrakno = ? where tarih = ? and sira = ?";
$insert_query = "insert into kasa values (?,?,?,?,?,?,?,?)";

$delete_query = "delete from kasa where tarih = ? and sira = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("sissddissi", $tarih, $sira, $hesapno, $aciklama, $gelir, $gider, $tip, $evrakno, $tarih, $sira);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("sissddis", $tarih, $sira, $hesapno, $aciklama, $gelir, $gider, $tip, $evrakno);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");
    

$delete_stmt->bind_param("si", $tarih, $sira);

$dbex->query("START TRANSACTION");

$sql = 'select * from kasa';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

while($row = $result->fetch_assoc()){
     $key = sprintf("%8s%5s", $row['tarih'], str_pad(number_format($row['sira'],0,'',''),5,'0',STR_PAD_LEFT));
     $val = sprintf("%4s%5s%-15s%-60s%15s%15s%2s%-15s", $row['tarih'],str_pad(number_format($row['tarih'],0,'',''),5,'0',STR_PAD_LEFT), toCP857($row['hesapno']),
            toCP857($row['aciklama']),str_pad(number_format($row['gelir'],2,'',''),15,'0',STR_PAD_LEFT),
            str_pad(number_format($row['gider'],2,'',''),15,'0',STR_PAD_LEFT), str_pad(number_format($row['tip'],0,'',''),2,'0',STR_PAD_LEFT),
            toCP857($row['evrakno']));
     
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $tarih = (string) rtrim(toUTF8(substr($data[$key],0,8)));
           $sira = floatVal(substr($data[$key],8,5));
           $hesapno = (string) rtrim(toUTF8(substr($data[$key],13,15)));
           $aciklama = (string) rtrim(toUTF8(substr($data[$key],28,60)));
           $gelir = floatVal(substr($data[$key],88,13) . '.' . substr($data[$key],101,2));
           $gider = floatVal(substr($data[$key],103,13) . '.' . substr($data[$key],116,2));
           $tip = floatVal(substr($data[$key],118,2));
           $evrakno = (string) rtrim(toUTF8(substr($data[$key],120,30)));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $tarih = $row['tarih']; $sira = $row['sira'];
         $delete_stmt->execute();
     }
}

foreach($data as $key => $val) {
   $tarih = (string) rtrim(toUTF8(substr($data[$key],0,8)));
   $sira = floatVal(substr($data[$key],8,5));
   $hesapno = (string) rtrim(toUTF8(substr($data[$key],13,15)));
   $aciklama = (string) rtrim(toUTF8(substr($data[$key],28,60)));
   $gelir = floatVal(substr($data[$key],88,13) . '.' . substr($data[$key],101,2));
   $gider = floatVal(substr($data[$key],103,13) . '.' . substr($data[$key],116,2));
   $tip = floatVal(substr($data[$key],118,2));
   $evrakno = (string) rtrim(toUTF8(substr($data[$key],120,30)));
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
