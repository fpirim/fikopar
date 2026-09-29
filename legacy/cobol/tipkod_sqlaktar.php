<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\TIPKOD.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS TIPKOD (
        kod int(4),
        tip varchar(40),
        PRIMARY KEY (kod)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update tipkod set kod = ?, tip = ? where kod = ? and tip = ?";
$insert_query = "insert into tipkod values (?,?)";

$delete_query = "delete from tipkod where kod = ? and tip = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("isis", $kod, $tip, $kod, $tip);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("is", $kod, $tip);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("is", $kod, $tip);

$dbex->query("START TRANSACTION");

$sql = 'select * from tipkod';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%4s", str_pad(number_format($row['kod'],0,'',''),4,'0',STR_PAD_LEFT));
     $val = sprintf("%4s%-40s", str_pad(number_format($row['kod'],0,'',''),4,'0',STR_PAD_LEFT), toCP857($row['tip']));
     
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $kod = floatVal(substr($data[$key],0,4));
           $tip = (string)  rtrim(toUTF8(substr($data[$key],4,40)));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $kod = $row['kod']; $tip = $row['tip'];
         $delete_stmt->execute();
     }
}

foreach($data as $key => $val) {
   $kod = floatVal(substr($data[$key],0,4));
   $tip = (string) rtrim(toUTF8(substr($data[$key],4,40)));
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
