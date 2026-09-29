<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\STOK.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS STOK (
        katno int(4),
        stno varchar(15),
        prcno varchar(30),
        oemno varchar(30),
        tipi varchar(30),
        cinsi varchar(60),
        marka varchar(30),
        min_adet numeric (20,4),
        max_adet numeric (20,4),
        fiyat numeric (14,4),
        note varchar(10),
        deg varchar(8),
        paket int(5),
        sira  int(15),
        PRIMARY KEY (katno,stno)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update stok set katno=?, stno=?, prcno=?, oemno=?, tipi=?,
                            cinsi = ?, marka = ?, min_adet = ?, max_adet = ?, fiyat = ?,
                            note = ?, deg = ?, paket = ?, sira = ? where katno = ? and stno = ?";
$insert_query = "insert into stok values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

$delete_query = "delete from stok where katno = ? and stno = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("issssssdddssiiis", $katno, $stno, $prcno, $oemno, $tipi, $cinsi, 
                                          $marka, $min_adet, $max_adet, $fiyat, $note, $deg, $paket, $sira, $katno, $stno);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("issssssdddssii", $katno, $stno, $prcno, $oemno, $tipi, $cinsi, 
                                          $marka, $min_adet, $max_adet, $fiyat, $note, $deg, $paket, $sira);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("is", $katno, $stno);

$dbex->query("START TRANSACTION");


$sql = 'select * from stok';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%4s%-15s", str_pad($row['katno'],4,'0',STR_PAD_LEFT), toCP857($row['stno']));
     $val = sprintf("%4s%-15s%-30s%-30s%-30s%-60s%-30s%-16s%-16s%14s%-10s%8s%5s%15s",
                 str_pad($row['katno'],4,'0',STR_PAD_LEFT), toCP857($row['stno']),
                 toCP857($row['prcno']), toCP857($row['oemno']), 
                 toCP857($row['tipi']),  toCP857($row['cinsi']), toCP857($row['marka']),
                 str_pad(number_format($row['min_adet'],4,'',''), 16, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['max_adet'],4,'',''), 16, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['fiyat'],4,'',''), 14, '0', STR_PAD_LEFT),
                 toCP857($row['note']),
                 str_pad($row['deg'],8,'0',STR_PAD_LEFT), 
                 str_pad($row['paket'],5,'0',STR_PAD_LEFT),
                 str_pad($row['sira'],15,'0',STR_PAD_LEFT)); 
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $katno = floatVal(substr($data[$key],0,4));
           $stno = (string)  rtrim(toUTF8(substr($data[$key],4,15)));
           $prcno = (string) rtrim(toUTF8(substr($data[$key],19,30)));
           $oemno = (string) rtrim(toUTF8(substr($data[$key],49,30)));
           $tipi = (string)  rtrim(toUTF8(substr($data[$key],79,30)));
           $cinsi = (string)  rtrim(toUTF8(substr($data[$key],109,60)));
           $marka =  (string) rtrim(toUTF8(substr($data[$key],169,30)));
           $min_adet = floatVal(substr($data[$key],199,12) . '.' . substr($data[$key],211,4));
           $max_adet = floatVal(substr($data[$key],215,12) . '.' . substr($data[$key],227,4));
           $fiyat = floatVal(substr($data[$key],231,10) . '.' . substr($data[$key],241,4));
           $note = (string) rtrim(toUTF8(substr($data[$key],245,10)));
           $deg = (string) rtrim(substr($data[$key],255,8));
           $paket = floatVal(substr($data[$key],263,5));
           $sira = floatVal(substr($data[$key],268,15));
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
    $prcno = (string) rtrim(toUTF8(substr($data[$key],19,30)));
    $oemno = (string) rtrim(toUTF8(substr($data[$key],49,30)));
    $tipi = (string)  rtrim(toUTF8(substr($data[$key],79,30)));
    $cinsi = (string)  rtrim(toUTF8(substr($data[$key],109,60)));
    $marka =  (string) rtrim(toUTF8(substr($data[$key],169,30)));
    $min_adet = floatVal(substr($data[$key],199,12) . '.' . substr($data[$key],211,4));
    $max_adet = floatVal(substr($data[$key],215,12) . '.' . substr($data[$key],227,4));
    $fiyat = floatVal(substr($data[$key],231,10) . '.' . substr($data[$key],241,4));
    $note = (string) rtrim(toUTF8(substr($data[$key],245,10)));
    $deg = (string) rtrim(substr($data[$key],255,8));
    $paket = floatVal(substr($data[$key],263,5));
    $sira = floatVal(substr($data[$key],268,15));
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
