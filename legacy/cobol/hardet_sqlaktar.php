<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\HARDET.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS HARDET (
        tip int(2),
        evrakno varchar(15),
        sira int(10),
        katno int(4),
        stno varchar(15),
        prcno varchar(30),
        tipi varchar(30),
        cinsi varchar(60),
        marka varchar(30),
        iskonto1 numeric (8,4),
        iskonto2 numeric (8,4),
        kdv numeric (8,4),
        adet numeric (16,4),
        fiyat numeric (14,4),
        PRIMARY KEY (tip,evrakno,sira)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update hardet set tip = ?, evrakno = ?, sira = ?, katno=?, stno=?, prcno=?,  tipi=?,
                            cinsi = ?, marka = ?, iskonto1 = ?, iskonto2 = ?, kdv = ?, adet = ?, fiyat = ?
                            where tip = ? and evrakno = ? and sira = ?";
$insert_query = "insert into hardet values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

$delete_query = "delete from hardet where tip = ? and evrakno = ? and sira = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("isiisssssdddddisi", $tip, $evrakno, $sira, $katno, $stno, $prcno, $tipi, $cinsi, 
                                          $marka, $iskonto1, $iskonto2, $kdv, $adet, $fiyat, $tip, $evrakno, $sira);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("isiisssssddddd", $tip, $evrakno, $sira, $katno, $stno, $prcno, $tipi, $cinsi, 
                                          $marka, $iskonto1, $iskonto2, $kdv, $adet, $fiyat);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("isi", $tip, $evrakno, $sira);

$dbex->query("START TRANSACTION");


$sql = 'select * from hardet';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%2s%-15s%10s", str_pad(number_format($row['tip'],0,'',''),2,'0',STR_PAD_LEFT), toCP857($row['evrakno']),
                                    str_pad(number_format($row['sira'],0,'',''),10,'0',STR_PAD_LEFT));
     $val = sprintf("%2s%-15s%10s%4s%-15s%-30s%-30s%-60s%-30s%8s%8s%8s%16s%14s",
                 str_pad(number_format($row['tip'],0,'',''),2,'0',STR_PAD_LEFT), toCP857($row['evrakno']), str_pad(number_format($row['sira'],0,'',''),10,'0',STR_PAD_LEFT),
                 str_pad($row['katno'],4,'0',STR_PAD_LEFT), toCP857($row['stno']),
                 toCP857($row['prcno']), toCP857($row['tipi']),  toCP857($row['cinsi']), toCP857($row['marka']),
                 str_pad(number_format($row['iskonto1'],4,'',''), 8, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['iskonto2'],4,'',''), 8, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['kdv'],4,'',''), 8, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['adet'],4,'',''), 16, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['fiyat'],4,'',''), 14, '0', STR_PAD_LEFT)); 
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $tip = floatVal(substr($data[$key],0,2));
           $evrakno = (string)  rtrim(toUTF8(substr($data[$key],2,15)));
           $sira = floatVal(substr($data[$key],17,10));
           $katno = floatVal(substr($data[$key],27,4));
           $stno = (string)  rtrim(toUTF8(substr($data[$key],31,15)));
           $prcno = (string) rtrim(toUTF8(substr($data[$key],46,30)));
           $tipi = (string)  rtrim(toUTF8(substr($data[$key],76,30)));
           $cinsi = (string)  rtrim(toUTF8(substr($data[$key],106,60)));
           $marka =  (string) rtrim(toUTF8(substr($data[$key],166,30)));
           $iskonto1 = floatVal(substr($data[$key],196,4) . '.' . substr($data[$key],200,4));
           $iskonto2 = floatVal(substr($data[$key],204,4) . '.' . substr($data[$key],208,4));
           $kdv = floatVal(substr($data[$key],212,4) . '.' . substr($data[$key],216,4));
           $adet = floatVal(substr($data[$key],220,12) . '.' . substr($data[$key],232,4));
           $fiyat = floatVal(substr($data[$key],236,10) . '.' . substr($data[$key],246,4));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $tip = $row['tip']; $evrakno = $row['evrakno']; $sira = $row['sira'];
         $delete_stmt->execute();
     }
}

foreach($data as $key => $val) {
   $tip = floatVal(substr($data[$key],0,2));
   $evrakno = (string)  rtrim(toUTF8(substr($data[$key],2,15)));
   $sira = floatVal(substr($data[$key],17,10));
   $katno = floatVal(substr($data[$key],27,4));
   $stno = (string)  rtrim(toUTF8(substr($data[$key],31,15)));
   $prcno = (string) rtrim(toUTF8(substr($data[$key],46,30)));
   $tipi = (string)  rtrim(toUTF8(substr($data[$key],76,30)));
   $cinsi = (string)  rtrim(toUTF8(substr($data[$key],106,60)));
   $marka =  (string) rtrim(toUTF8(substr($data[$key],166,30)));
   $iskonto1 = floatVal(substr($data[$key],196,4) . '.' . substr($data[$key],200,4));
   $iskonto2 = floatVal(substr($data[$key],204,4) . '.' . substr($data[$key],208,4));
   $kdv = floatVal(substr($data[$key],212,4) . '.' . substr($data[$key],216,4));
   $adet = floatVal(substr($data[$key],220,12) . '.' . substr($data[$key],232,4));
   $fiyat = floatVal(substr($data[$key],236,10) . '.' . substr($data[$key],246,4));
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
