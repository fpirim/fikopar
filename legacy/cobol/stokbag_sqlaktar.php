<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\STOKBAG.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}
      
$sql = "CREATE TABLE IF NOT EXISTS STOKBAG (
        katno int(4),
        stno varchar(15),
        sira int(12),
        bkatno int(4),
        bstno varchar(15),
        isk numeric(9,4),
        kdv numeric (9,4),
        PRIMARY KEY (katno,stno,sira)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update stokbag set katno=?, stno=?, sira=?, bkatno=?, bstno=?, isk=?, kdv=? where katno=? and stno=? and sira=?";
$insert_query = "insert into stokbag values (?,?,?,?,?,?,?)";

$delete_query = "delete from stokbag where katno=? and stno=? and sira=?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("isiisddisi", $katno, $stno, $sira, $bkatno, $bstno, $isk, $kdv, $katno, $stno, $sira);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("isiisdd", $katno, $stno, $sira, $bkatno, $bstno, $isk, $kdv);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("isi", $katno, $stno, $sira);

$dbex->query("START TRANSACTION");


$sql = 'select * from stokbag';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%4s%-15s%12s", 
            str_pad($row['katno'],4,'0',STR_PAD_LEFT), toCP857($row['stno']), 
            str_pad($row['sira'],12,'0',STR_PAD_LEFT));
     $val = sprintf("%4s%-15s%12s%4s%-15s%9s%9s",
                 str_pad($row['katno'],4,'0',STR_PAD_LEFT), toCP857($row['stno']), 
            str_pad($row['sira'],12,'0',STR_PAD_LEFT),str_pad($row['bkatno'],4,'0',STR_PAD_LEFT),toCP857($row['bstno']),
            str_pad(number_format($row['isk'],4,'',''), 9, '0', STR_PAD_LEFT),
            str_pad(number_format($row['kdv'],4,'',''), 9, '0', STR_PAD_LEFT)); 
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $katno = floatVal(substr($data[$key],0,4));
           $stno = (string)  rtrim(toUTF8(substr($data[$key],4,15)));
           $sira = floatVal(substr($data[$key],19,12));
           $bkatno = floatVal(substr($data[$key],31,4));
           $bstno = (string)  rtrim(toUTF8(substr($data[$key],35,15)));
           $isk = floatVal(substr($data[$key],50,5) . '.' . substr($data[$key],55,4) );
           $kdv = floatVal(substr($data[$key],59,5) . '.' . substr($data[$key],64,4) );
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $katno = $row['katno']; $stno = $row['stno']; $sira = $row['sira'];
         $delete_stmt->execute();
     }
}
foreach($data as $key => $val) {
   $katno = floatVal(substr($data[$key],0,4));
   $stno = (string)  rtrim(toUTF8(substr($data[$key],4,15)));
   $sira = floatVal(substr($data[$key],19,12));
   $bkatno = floatVal(substr($data[$key],31,4));
   $bstno = (string)  rtrim(toUTF8(substr($data[$key],35,15)));
   $isk = floatVal(substr($data[$key],50,5) . '.' . substr($data[$key],55,4) );
   $kdv = floatVal(substr($data[$key],59,5) . '.' . substr($data[$key],64,4) );
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
