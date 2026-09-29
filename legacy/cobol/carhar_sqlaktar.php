<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\CARHAR.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS CARHAR (
        hesapno varchar(15),
        depono int(4),
        tarih varchar(8),
        sira int(10),
        tip int(2),
        evrakno varchar(15),
        aciklama varchar(60),
        vade varchar(15),
        borc numeric (15,2),
        alacak numeric (15,2),
        PRIMARY KEY (hesapno,depono,tarih,sira)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update carhar set hesapno = ?, depono = ?, tarih = ?, sira = ?, tip = ?, evrakno = ?,
                            aciklama = ?, vade = ?, borc = ?, alacak = ?
                            where hesapno = ? and depono = ? and tarih = ? and sira = ?";
$insert_query = "insert into carhar values (?,?,?,?,?,?,?,?,?,?)";

$delete_query = "delete from carhar where hesapno = ? and depono = ? and tarih = ? and sira = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("sisissssddsisi", $hesapno, $depono, $tarih, $sira, $tip, $evrakno, $aciklama, $vade, 
                                          $borc, $alacak, $hesapno, $depono, $tarih, $sira);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("sisissssdd", $hesapno, $depono, $tarih, $sira, $tip, $evrakno, $aciklama, $vade, $borc, $alacak);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("sisi", $hesapno, $depono, $tarih, $sira);

$dbex->query("START TRANSACTION");


$sql = 'select * from carhar';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

           
while($row = $result->fetch_assoc()){
     $key = sprintf("%-15s%4s%8s%10s", toCP857($row['hesapno']),
                     str_pad(number_format($row['depono'],0,'',''),4,'0',STR_PAD_LEFT), 
                     str_pad(number_format($row['tarih'],0,'',''),8,'0',STR_PAD_LEFT),
                     str_pad(number_format($row['sira'],0,'',''),10,'0',STR_PAD_LEFT));
     $val = sprintf("%-15s%4s%8s%10s%2s%-15s%-60s%-15s%15s%15s",
                     toCP857($row['hesapno']),
                     str_pad(number_format($row['depono'],0,'',''),4,'0',STR_PAD_LEFT), 
                     str_pad(number_format($row['tarih'],0,'',''),8,'0',STR_PAD_LEFT),
                     str_pad(number_format($row['sira'],0,'',''),10,'0',STR_PAD_LEFT),
                     str_pad(number_format($row['tip'],0,'',''),2,'0',STR_PAD_LEFT),
                     toCP857($row['evrakno']),
                     toCP857($row['aciklama']),
                     toCP857($row['vade']),
                     str_pad(number_format($row['borc'],2,'',''),15,'0',STR_PAD_LEFT),
                     str_pad(number_format($row['alacak'],2,'',''),15,'0',STR_PAD_LEFT)); 
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $hesapno = (string)  rtrim(toUTF8(substr($data[$key],0,15)));
           $depono = floatVal(substr($data[$key],15,4));
           $tarih = (string)  rtrim(toUTF8(substr($data[$key],19,8)));
           $sira = floatVal(substr($data[$key],27,10));
           $tip = floatVal(substr($data[$key],37,2));
           $evrakno = (string)  rtrim(toUTF8(substr($data[$key],39,15)));
           $aciklama = (string) rtrim(toUTF8(substr($data[$key],54,60)));
           $vade = (string)  rtrim(toUTF8(substr($data[$key],114,15)));
           $borc = floatVal(substr($data[$key],129,13) . '.' . substr($data[$key],142,2));
           $alacak = floatVal(substr($data[$key],144,13) . '.' . substr($data[$key],157,2));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $hesapno = $row['hesapno']; $depono = $row['depono']; $tarih = $row['tarih']; $sira = $row['sira'];
         $delete_stmt->execute();
     }
}

foreach($data as $key => $val) {
   $hesapno = (string)  rtrim(toUTF8(substr($data[$key],0,15)));
   $depono = floatVal(substr($data[$key],15,4));
   $tarih = (string)  rtrim(toUTF8(substr($data[$key],19,8)));
   $sira = floatVal(substr($data[$key],27,10));
   $tip = floatVal(substr($data[$key],37,2));
   $evrakno = (string)  rtrim(toUTF8(substr($data[$key],39,15)));
   $aciklama = (string) rtrim(toUTF8(substr($data[$key],54,60)));
   $vade = (string)  rtrim(toUTF8(substr($data[$key],114,15)));
   $borc = floatVal(substr($data[$key],129,13) . '.' . substr($data[$key],142,2));
   $alacak = floatVal(substr($data[$key],144,13) . '.' . substr($data[$key],157,2));
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
