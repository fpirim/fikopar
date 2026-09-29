<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\HARSIC.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS HARSIC (
        tip int(2),
        evrakno varchar(15),
        tarih varchar(8),
        saat varchar(6),
        ftarih varchar(8),
        hesapno varchar(15),
        hesapadi varchar(50),
        yetkili varchar(40),
        adres1 varchar(40),
        adres2 varchar(40),
        adres3 varchar(40),
        mahalle varchar(20),
        ilce varchar(20),
        il varchar(20),
        ulke varchar(20),
        vda varchar(30),
        vno varchar(10),
        tcno varchar(11),
        tsno varchar(11),
        mersis varchar(20),
        gun int(5),
        plkod int(4),
        depono int(4),
        duzen int(4),
        bag1 int(2),
        bevrakno1 varchar(15),
        bag2 int(2),
        bevrakno2 varchar(15),
        sonuc int(2),
        PRIMARY KEY (tip, evrakno)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update harsic set tip = ?, evrakno = ?, tarih = ?, saat = ?, ftarih = ?, hesapno=?, hesapadi=?, yetkili=?, adres1=?, adres2=?,
                            adres3 = ?, mahalle = ?, ilce = ?, il = ?,
                            ulke = ?, vda = ?, vno = ?, tcno = ?, tsno = ?, mersis = ?, gun = ?, plkod = ?, depono = ?,
                            duzen = ?, bag1 = ?, bevrakno1 = ?, bag2 = ?, bevrakno2 = ?, sonuc = ? where tip = ? and evrakno = ?";
$insert_query = "insert into harsic values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

$delete_query = "delete from harsic where tip = ? and evrakno = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("isssssssssssssssssssiiiiisisiis", $tip, $evrakno, $tarih, $saat, $ftarih, $hesapno, $hesapadi, $yetkili, $adres1, $adres2, $adres3, $mahalle, 
                                          $ilce, $il, $ulke, $vda, $vno, $tcno, $tsno, $mersis, $gun, $plkod, $depono, $duzen, $bag1, $bevrakno1, $bag2, $bevrakno2,
                                          $sonuc, $tip, $evrakno);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("isssssssssssssssssssiiiiisisi", $tip, $evrakno, $tarih, $saat, $ftarih, $hesapno, $hesapadi, $yetkili, $adres1, $adres2, $adres3,  $mahalle, 
                                          $ilce, $il, $ulke, $vda, $vno, $tcno, $tsno, $mersis, $gun, $plkod, $depono, $duzen, $bag1, $bevrakno1, $bag2, $bevrakno2,
                                          $sonuc);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("is", $tip, $evkrano);

$dbex->query("START TRANSACTION");


$sql = 'select * from harsic';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%2s%-15s", str_pad($row['tip'], 2, '0', STR_PAD_LEFT), toCP857($row['evrakno']));
     $val = sprintf("%2s%-15s%8s%6s%8s%-15s%-50s%-40s%-40s%-40s%-40s%-20s%-20s%-20s%-20s%-30s%-10s%-11s%-11s%-20s%5s%4s%4s%4s%2s%-15s%2s%-15s%2s",
                 str_pad($row['tip'], 2, '0', STR_PAD_LEFT),
                 toCP857($row['evrakno']), $row['tarih'], $row['saat'], $row['ftarih'],
                 toCP857($row['hesapno']),toCP857($row['hesapadi']),toCP857($row['yetkili']),toCP857($row['adres1']),toCP857($row['adres2']),toCP857($row['adres3']),
                 toCP857($row['mahalle']),toCP857($row['ilce']),toCP857($row['il']),toCP857($row['ulke']),toCP857($row['vda']),toCP857($row['vno']),
                 toCP857($row['tcno']),toCP857($row['tsno']),toCP857($row['mersis']),
                 str_pad(number_format($row['gun'],0,'',''), 5, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['plkod'],0,'',''), 4, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['depono'],0,'',''), 4, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['duzen'],0,'',''), 4, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['bag1'],0,'',''), 2, '0', STR_PAD_LEFT), toCP857($row['bevrakno1']),
                 str_pad(number_format($row['bag2'],0,'',''), 4, '0', STR_PAD_LEFT), toCP857($row['bevrakno2']),
                 str_pad(number_format($row['sonuc'],0,'',''), 4, '0', STR_PAD_LEFT)); 
                 
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $tip = floatVal(substr($data[$key],0,2));
           $evrakno = (string) rtrim(substr($data[$key],2,15));
           $tarih = (string) rtrim(substr($data[$key],17,8));
           $saat = (string) rtrim(substr($data[$key],25,6));
           $ftarih = (string) rtrim(substr($data[$key],31,8));
           $hesapno = (string) rtrim(substr($data[$key],39,15));
           $hesapadi = (string)  rtrim(toUTF8(substr($data[$key],54,50)));
           $yetkili = (string) rtrim(toUTF8(substr($data[$key],104,40)));
           $adres1 = (string) rtrim(toUTF8(substr($data[$key],144,40)));
           $adres2 = (string) rtrim(toUTF8(substr($data[$key],184,40)));
           $adres3 = (string) rtrim(toUTF8(substr($data[$key],224,40)));
           $mahalle = (string) rtrim(toUTF8(substr($data[$key],264,20)));
           $ilce = (string) rtrim(toUTF8(substr($data[$key],284,20)));
           $il = (string) rtrim(toUTF8(substr($data[$key],304,20)));
           $ulke = (string) rtrim(toUTF8(substr($data[$key],324,20)));
           $vda = (string) rtrim(toUTF8(substr($data[$key],344,30)));
           $vno = (string) rtrim(toUTF8(substr($data[$key],374,10)));
           $tcno = (string) rtrim(toUTF8(substr($data[$key],384,11)));
           $tsno = (string) rtrim(toUTF8(substr($data[$key],395,11)));
           $mersis = (string) rtrim(toUTF8(substr($data[$key],406,20)));
           $gun = floatVal(substr($data[$key],426,5));
           $plkod = floatVal(substr($data[$key],431,4));
           $depono = floatVal(substr($data[$key],435,4));
           $duzen = floatVal(substr($data[$key],439,4));
           $bag1 = floatVal(substr($data[$key],443,2));
           $bevrakno1 = (string) rtrim(toUTF8(substr($data[$key],445,15)));
           $bag2 = floatVal(substr($data[$key],460,2));
           $bevrakno2 = (string) rtrim(toUTF8(substr($data[$key],462,15)));
           $sonuc = floatVal(substr($data[$key],477,2));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $hesapno = $row['hesapno'];
         $delete_stmt->execute();
     }
}
foreach($data as $key => $val) {
   $tip = floatVal(substr($data[$key],0,2));
   $evrakno = (string) rtrim(substr($data[$key],2,15));
   $tarih = (string) rtrim(substr($data[$key],17,8));
   $saat = (string) rtrim(substr($data[$key],25,6));
   $ftarih = (string) rtrim(substr($data[$key],31,8));
   $hesapno = (string) rtrim(substr($data[$key],39,15));
   $hesapadi = (string)  rtrim(toUTF8(substr($data[$key],54,50)));
   $yetkili = (string) rtrim(toUTF8(substr($data[$key],104,40)));
   $adres1 = (string) rtrim(toUTF8(substr($data[$key],144,40)));
   $adres2 = (string) rtrim(toUTF8(substr($data[$key],184,40)));
   $adres3 = (string) rtrim(toUTF8(substr($data[$key],224,40)));
   $mahalle = (string) rtrim(toUTF8(substr($data[$key],264,20)));
   $ilce = (string) rtrim(toUTF8(substr($data[$key],284,20)));
   $il = (string) rtrim(toUTF8(substr($data[$key],304,20)));
   $ulke = (string) rtrim(toUTF8(substr($data[$key],324,20)));
   $vda = (string) rtrim(toUTF8(substr($data[$key],344,30)));
   $vno = (string) rtrim(toUTF8(substr($data[$key],374,10)));
   $tcno = (string) rtrim(toUTF8(substr($data[$key],384,11)));
   $tsno = (string) rtrim(toUTF8(substr($data[$key],395,11)));
   $mersis = (string) rtrim(toUTF8(substr($data[$key],406,20)));
   $gun = floatVal(substr($data[$key],426,5));
   $plkod = floatVal(substr($data[$key],431,4));
   $depono = floatVal(substr($data[$key],435,4));
   $duzen = floatVal(substr($data[$key],439,4));
   $bag1 = floatVal(substr($data[$key],443,2));
   $bevrakno1 = (string) rtrim(toUTF8(substr($data[$key],445,15)));
   $bag2 = floatVal(substr($data[$key],460,2));
   $bevrakno2 = (string) rtrim(toUTF8(substr($data[$key],462,15)));
   $sonuc = floatVal(substr($data[$key],477,2));
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
