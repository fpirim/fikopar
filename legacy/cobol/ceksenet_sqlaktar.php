<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\CEKSENET.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS CEKSENET (
        evrakno varchar(15),
        tip int(2),
        hesapno varchar(15),
        depono int(4),
        vade varchar(8),
        tarih varchar(8),
        tutar numeric(15,2),
        khesapno varchar(15),
        khesapadi varchar(50),
        kyetkili varchar(40),
        kbankasehir varchar(20),
        ksubeilce varchar(20),
        kbankanotcno varchar(20),
        kceksenetno varchar(20),
        PRIMARY KEY (evrakno, tip)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update ceksenet set evrakno = ?, tip = ?, hesapno = ?, depono = ?, vade = ?, tarih = ?, tutar = ?, 
                            khesapno = ?, khesapadi = ?, kyetkili = ?, kbankasehir = ?, ksubeilce = ?, 
                            kbankanotcno = ?, kceksenetno = ? where evrakno = ? and tip = ?";
$insert_query = "insert into ceksenet values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

$delete_query = "delete from ceksenet where evrakno = ? and tip = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("sisissdssssssssi", $evrakno, $tip, $hesapno, $depono, $vade, $tarih, $tutar,
                    $khesapno, $khesapadi, $kyetkili, $kbankasehir, $ksubeilce, $kbankanotcno, $kceksenetno, $evrakno, $tip);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("sisissdsssssss", $evrakno, $tip, $hesapno, $depono, $vade, $tarih, $tutar,
                    $khesapno, $khesapadi, $kyetkili, $kbankasehir, $ksubeilce, $kbankanotcno, $kceksenetno);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("si", $evrakno, $tip);

$dbex->query("START TRANSACTION");


$sql = 'select * from ceksenet';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%-15s%2s", toCP857($row['evrakno']), str_pad($row['tip'], 2, '0', STR_PAD_LEFT));
     $val = sprintf("%-15s%2s%-15s%4s%8s%8s%15s%-15s%-50s%-40s%-20s%-20s%-20s%-20s",
                 toCP857($row['evrakno']), str_pad($row['tip'], 2, '0', STR_PAD_LEFT),
                 toCP857($row['hesapno']), str_pad($row['depono'], 4, '0', STR_PAD_LEFT),
                 $row['vade'],$row['tarih'], 
                 str_pad(number_format($row['tutar'],2,'',''), 15, '0', STR_PAD_LEFT),
                 toCP857($row['khesapno']),toCP857($row['khesapadi']),toCP857($row['kyetkili']),
                 toCP857($row['kbankasehir']),toCP857($row['ksubeilce']),toCP857($row['kbankanotcno']),toCP857($row['kceksenetno'])); 
                 
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $evrakno = (string) rtrim(substr($data[$key],0,15));
           $tip = floatVal(substr($data[$key],15,2));
           $hesapno = (string) rtrim(substr($data[$key],17,15));
           $depono = floatVal(substr($data[$key],32,4));
           $vade = (string) rtrim(substr($data[$key],36,8));
           $tarih = (string) rtrim(substr($data[$key],44,8));
           $tutar = floatVal(substr($data[$key],52,13) . '.' . substr($data[$key],65,2));
           $khesapno = (string) rtrim(toUTF8(substr($data[$key],67,15)));
           $khesapadi = (string)  rtrim(toUTF8(substr($data[$key],82,50)));
           $kyetkili = (string) rtrim(toUTF8(substr($data[$key],132,40)));
           $kbankasehir = (string) rtrim(toUTF8(substr($data[$key],172,20)));
           $ksubeilce = (string) rtrim(toUTF8(substr($data[$key],192,20)));
           $kbankanotcno = (string) rtrim(toUTF8(substr($data[$key],212,20)));
           $kceksenetno = (string) rtrim(toUTF8(substr($data[$key],232,20)));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $evrakno = $row['evrakno']; $tip = $row['tip'];
         $delete_stmt->execute();
     }
}
foreach($data as $key => $val) {
$evrakno = (string) rtrim(substr($data[$key],0,15));
   $tip = floatVal(substr($data[$key],15,2));
   $hesapno = (string) rtrim(substr($data[$key],17,15));
   $depono = floatVal(substr($data[$key],32,4));
   $vade = (string) rtrim(substr($data[$key],36,8));
   $tarih = (string) rtrim(substr($data[$key],44,8));
   $tutar = floatVal(substr($data[$key],52,13) . '.' . substr($data[$key],65,2));
   $khesapno = (string) rtrim(toUTF8(substr($data[$key],67,15)));
   $khesapadi = (string)  rtrim(toUTF8(substr($data[$key],82,50)));
   $kyetkili = (string) rtrim(toUTF8(substr($data[$key],132,40)));
   $kbankasehir = (string) rtrim(toUTF8(substr($data[$key],172,20)));
   $ksubeilce = (string) rtrim(toUTF8(substr($data[$key],192,20)));
   $kbankanotcno = (string) rtrim(toUTF8(substr($data[$key],212,20)));
   $kceksenetno = (string) rtrim(toUTF8(substr($data[$key],232,20)));
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
