<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\KATLOG.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}
      
$sql = "CREATE TABLE IF NOT EXISTS KATLOG (
        katno int(4),
        aciklama varchar(60),
        hesapno varchar(15),
        PRIMARY KEY (katno)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update katlog set katno=?, aciklama=?, hesapno=? where katno = ? ";
$insert_query = "insert into katlog values (?,?,?)";

$delete_query = "delete from katlog where katno = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("issi", $katno, $aciklama, $hesapno, $katno);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("iss", $katno, $aciklama, $hesapno);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("i", $katno);

$dbex->query("START TRANSACTION");


$sql = 'select * from katlog';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%4s", str_pad($row['katno'],4,'0',STR_PAD_LEFT));
     $val = sprintf("%4s%-60s%-15s",
                 str_pad($row['katno'],4,'0',STR_PAD_LEFT), toCP857($row['aciklama']),
                 toCP857($row['hesapno'])); 
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $katno = floatVal(substr($data[$key],0,4));
           $aciklama = (string)  rtrim(toUTF8(substr($data[$key],4,60)));
           $hesapno = (string) rtrim(toUTF8(substr($data[$key],64,15)));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $katno = $row['katno'];
         $delete_stmt->execute();
     }
}
foreach($data as $key => $val) {
    $katno = floatVal(substr($data[$key],0,4));
    $aciklama = (string)  rtrim(toUTF8(substr($data[$key],4,60)));
    $hesapno = (string) rtrim(toUTF8(substr($data[$key],64,15)));
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
