<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\PLASIYER.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS PLASIYER (
        plno int(4),
        pladi varchar(60),
        PRIMARY KEY (plno)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update plasiyer set plno = ?, pladi = ? where plno = ? and pladi = ?";
$insert_query = "insert into plasiyer values (?,?)";

$delete_query = "delete from plasiyer where plno = ? and pladi = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("isis", $plno, $pladi, $plno, $pladi);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("is", $plno, $pladi);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("is", $plno, $pladi);

$dbex->query("START TRANSACTION");

$sql = 'select * from plasiyer';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%4s", str_pad(number_format($row['plno'],0,'',''),4,'0',STR_PAD_LEFT));
     $val = sprintf("%4s%-60s", str_pad(number_format($row['plno'],0,'',''),4,'0',STR_PAD_LEFT), toCP857($row['pladi']));
     
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $plno = floatVal(substr($data[$key],0,4));
           $pladi = (string)  rtrim(toUTF8(substr($data[$key],4,60)));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $plno = $row['plno']; $pladi = $row['pladi'];
         $delete_stmt->execute();
     }
}

foreach($data as $key => $val) {
   $plno = floatVal(substr($data[$key],0,4));
   $pladi = (string)  rtrim(toUTF8(substr($data[$key],4,60)));
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
