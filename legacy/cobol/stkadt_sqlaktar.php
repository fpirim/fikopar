<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\STKADT.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS STKADT (
        katno int(4),
        stno varchar(15),
        yil int(4),
        depono int(4),
        gir numeric(16,4),
        cik numeric(16,4),
        PRIMARY KEY (katno,yil,stno,depono)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update stkadt set katno=?, stno = ?, yil = ?,  depono = ?, gir = ?, cik = ?
                 where katno = ? and stno = ? and yil = ? and depono = ?";
$insert_query = "insert into stkadt values (?,?,?,?,?,?)";

$delete_query = "delete from stkadt where katno = ? and stno = ? and yil = ? and depono = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("isiiddisii", $katno, $stno, $yil,  $depono, $gir, $cik, $katno, $stno, $yil,  $depono);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("isiidd", $katno, $stno, $yil, $depono, $gir, $cik);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("isii", $katno, $stno, $yil, $depono);

$dbex->query("START TRANSACTION");


$sql = 'select * from stkadt';

if(!$result = $db->query($sql)) {
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()) {
     $key = sprintf("%4s%-15s%4s%4s", 
            str_pad($row['katno'],4,'0',STR_PAD_LEFT), 
            toCP857($row['stno']),
            str_pad($row['yil'],4,'0',STR_PAD_LEFT), 
            str_pad($row['depono'],4,'0',STR_PAD_LEFT));

     $val = sprintf("%4s%-15s%4s%4s%16s%16s",
            str_pad($row['katno'],4,'0',STR_PAD_LEFT), 
            toCP857($row['stno']),
            str_pad($row['yil'],4,'0',STR_PAD_LEFT), 
            str_pad($row['depono'],4,'0',STR_PAD_LEFT),
            str_pad(number_format($row['gir'],4,'',''), 16, '0', STR_PAD_LEFT),
            str_pad(number_format($row['cik'],4,'',''), 16, '0', STR_PAD_LEFT)); 
            
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $katno = floatVal(substr($data[$key],0,4));
           $stno = (string)  rtrim(toUTF8(substr($data[$key],4,15)));
           $yil = floatVal(substr($data[$key],19,4));
           $depono = floatVal(substr($data[$key],23,4));
           $gir = floatVal(substr($data[$key],27,12) . '.' . substr($data[$key],39,4));
           $cik = floatVal(substr($data[$key],43,12) . '.' . substr($data[$key],55,4));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $stno = $row['stno']; $katno = $row['katno'];
         $delete_stmt->execute();
     }
}
foreach($data as $key => $val) {
   $katno = floatVal(substr($data[$key],0,4));
   $stno = (string)  rtrim(toUTF8(substr($data[$key],4,15)));
   $yil = floatVal(substr($data[$key],19,4));
   $depono = floatVal(substr($data[$key],23,4));
   $gir = floatVal(substr($data[$key],27,12) . '.' . substr($data[$key],39,4));
   $cik = floatVal(substr($data[$key],43,12) . '.' . substr($data[$key],55,4));
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
