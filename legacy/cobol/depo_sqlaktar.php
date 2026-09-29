<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\DEPO.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS DEPO (
        dpno int(4),
        adi varchar(30),
        kdv numeric(8,4),
        PRIMARY KEY (dpno)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update depo set dpno = ?, adi = ?, kdv = ? where dpno = ?";
$insert_query = "insert into depo values (?,?,?)";

$delete_query = "delete from depo where dpno = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("isdi", $dpno, $adi, $kdv, $dpno);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("isd", $dpno, $adi, $kdv);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");
    

$delete_stmt->bind_param("i", $dpno);

$dbex->query("START TRANSACTION");

$sql = 'select * from depo';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%4s", str_pad(number_format($row['dpno'],0,'',''),4,'0',STR_PAD_LEFT));
     $val = sprintf("%4s%-30s%8s", str_pad(number_format($row['dpno'],0,'',''),4,'0',STR_PAD_LEFT), toCP857($row['adi']),
            str_pad(number_format($row['kdv'],4,'',''),8,'0',STR_PAD_LEFT));
     
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $dpno = floatVal(substr($data[$key],0,4));
           $adi = (string) rtrim(toUTF8(substr($data[$key],4,30)));
           $kdv = floatVal(substr($data[$key],34,4) . '.' . substr($data[$key],38,4));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $dpno = $row['dpno'];
         $delete_stmt->execute();
     }
}

foreach($data as $key => $val) {
   $dpno = floatVal(substr($data[$key],0,4));
   $adi = (string) rtrim(toUTF8(substr($data[$key],4,30)));
   $kdv = floatVal(substr($data[$key],34,4) . '.' . substr($data[$key],38,4));
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
