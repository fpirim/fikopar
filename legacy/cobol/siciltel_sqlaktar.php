<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\SICILTEL.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS SICILTEL (
        hesapno varchar(15),
        sira int(10),
        tel int(12),
        dahili varchar(5),
        tip int(1),
        PRIMARY KEY (hesapno,sira)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update siciltel set hesapno = ?, sira = ?, tel=?, dahili=?, tip=? where hesapno = ? and sira = ?";
$insert_query = "insert into siciltel values (?,?,?,?,?)";

$delete_query = "delete from siciltel where hesapno = ? and sira = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("siisisi", $hesapno, $sira, $tel, $dahili, $tip, $hesapno, $sira);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("siisi", $hesapno, $sira, $tel, $dahili, $tip);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("si", $hesapno, $sira);

$dbex->query("START TRANSACTION");


$sql = 'select * from siciltel';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%-15s%10s", toCP857($row['hesapno']), str_pad(number_format($row['sira'],0,'',''),10,'0',STR_PAD_LEFT));
     $val = sprintf("%-15s%10s%12s%-5s%-1s",
                 toCP857($row['hesapno']), str_pad(number_format($row['sira'],0,'',''),10,'0',STR_PAD_LEFT),
                 str_pad(number_format($row['tel'],0,'',''),12,'0',STR_PAD_LEFT),
                 toCP857($row['dahili']),
                 str_pad(number_format($row['tip'],0,'',''),1,'0',STR_PAD_LEFT)); 
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $hesapno = (string)  rtrim(toUTF8(substr($data[$key],0,15)));
           $sira = floatVal(substr($data[$key],15,10));
           $tel = floatVal(substr($data[$key],25,12));
           $dahili = (string)  rtrim(toUTF8(substr($data[$key],37,5)));
           $tip = floatVal(substr($data[$key],42,1));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $hesapno = $row['hesapno']; $sira = $row['sira'];
         $delete_stmt->execute();
     }
}

foreach($data as $key => $val) {
   $hesapno = (string)  rtrim(toUTF8(substr($data[$key],0,15)));
   $sira = floatVal(substr($data[$key],15,10));
   $tel = floatVal(substr($data[$key],25,12));
   $dahili = (string)  rtrim(toUTF8(substr($data[$key],37,5)));
   $tip = floatVal(substr($data[$key],42,1));
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
