<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\SICIL.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS SICIL (
        hesapno varchar(15),
        hesapadi varchar(50),
        yetkili varchar(40),
        adres1 varchar(40),
        adres2 varchar(40),
        adres3 varchar(40),
        poskod varchar(6),
        mahalle varchar(20),
        ilce varchar(20),
        il varchar(20),
        ulke varchar(20),
        vda varchar(30),
        vno varchar(10),
        tcno varchar(11),
        tsno varchar(11),
        mersis varchar(20),
        tur varchar(3),
        kredi numeric(18,4),
        gun int(5),
        iskonto1 numeric(8,4),
        iskonto2 numeric(8,4),
        plkod int(4),
        PRIMARY KEY (hesapno)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update sicil set hesapno=?, hesapadi=?, yetkili=?, adres1=?, adres2=?,
                            adres3 = ?, poskod = ?, mahalle = ?, ilce = ?, il = ?,
                            ulke = ?, vda = ?, vno = ?, tcno = ?, tsno = ?, mersis = ?, tur = ?, kredi = ?, gun = ?,
                            iskonto1 = ?, iskonto2 = ?, plkod = ? where hesapno = ?";
$insert_query = "insert into sicil values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

$delete_query = "delete from sicil where hesapno = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("sssssssssssssssssdiddis", $hesapno, $hesapadi, $yetkili, $adres1, $adres2, $adres3, $poskod, $mahalle, 
                                          $ilce, $il, $ulke, $vda, $vno, $tcno, $tsno, $mersis, $tur, $kredi, $gun, $iskonto1,
                                          $iskonto2, $plkod, $hesapno);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("sssssssssssssssssdiddi", $hesapno, $hesapadi, $yetkili, $adres1, $adres2, $adres3, $poskod, $mahalle, 
                                          $ilce, $il, $ulke, $vda, $vno, $tcno, $tsno, $mersis, $tur, $kredi, $gun, $iskonto1,
                                          $iskonto2, $plkod);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("s", $hesapno);

$dbex->query("START TRANSACTION");


$sql = 'select * from sicil';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%-15s", toCP857($row['hesapno']));
     $val = sprintf("%-15s%-50s%-40s%-40s%-40s%-40s%-6s%-20s%-20s%-20s%-20s%-30s%-10s%-11s%-11s%-20s%-3s%14s%5s%8s%8s%4s",
                 toCP857($row['hesapno']),toCP857($row['hesapadi']),toCP857($row['yetkili']),toCP857($row['adres1']),toCP857($row['adres2']),toCP857($row['adres3']),
                 toCP857($row['poskod']),toCP857($row['mahalle']),toCP857($row['ilce']),toCP857($row['il']),toCP857($row['ulke']),toCP857($row['vda']),toCP857($row['vno']),
                 toCP857($row['tcno']),toCP857($row['tsno']),toCP857($row['mersis']),toCP857($row['tur']),
                 str_pad(number_format($row['kredi'],4,'',''), 14, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['gun'],0,'',''), 5, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['iskonto1'],4,'',''), 8, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['iskonto2'],4,'',''), 8, '0', STR_PAD_LEFT),
                 str_pad(number_format($row['plkod'],0,'',''), 4, '0', STR_PAD_LEFT)); 
                 
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $hesapno = (string) rtrim(substr($data[$key],0,15));
           $hesapadi = (string)  rtrim(toUTF8(substr($data[$key],15,50)));
           $yetkili = (string) rtrim(toUTF8(substr($data[$key],65,40)));
           $adres1 = (string) rtrim(toUTF8(substr($data[$key],105,40)));
           $adres2 = (string) rtrim(toUTF8(substr($data[$key],145,40)));
           $adres3 = (string) rtrim(toUTF8(substr($data[$key],185,40)));
           $poskod = (string) rtrim(toUTF8(substr($data[$key],225,6)));
           $mahalle = (string) rtrim(toUTF8(substr($data[$key],231,20)));
           $ilce = (string) rtrim(toUTF8(substr($data[$key],251,20)));
           $il = (string) rtrim(toUTF8(substr($data[$key],271,20)));
           $ulke = (string) rtrim(toUTF8(substr($data[$key],291,20)));
           $vda = (string) rtrim(toUTF8(substr($data[$key],311,30)));
           $vno = (string) rtrim(toUTF8(substr($data[$key],341,10)));
           $tcno = (string) rtrim(toUTF8(substr($data[$key],351,11)));
           $tsno = (string) rtrim(toUTF8(substr($data[$key],362,11)));
           $mersis = (string) rtrim(toUTF8(substr($data[$key],373,20)));
           $tur = (string) rtrim(toUTF8(substr($data[$key],393,3)));
           $kredi = floatVal(substr($data[$key],396,14) . '.' . substr($data[$key],410,4));
           $gun = floatVal(substr($data[$key],414,5));
           $iskonto1 = floatVal(substr($data[$key],419,4) . '.' . substr($data[$key],423,4));
           $iskonto2 = floatVal(substr($data[$key],427,4) . '.' . substr($data[$key],431,4));
           $plkod = floatVal(substr($data[$key],435,4));
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
   $hesapno = (string) rtrim(substr($data[$key],0,15));
   $hesapadi = (string)  rtrim(toUTF8(substr($data[$key],15,50)));
   $yetkili = (string) rtrim(toUTF8(substr($data[$key],65,40)));
   $adres1 = (string) rtrim(toUTF8(substr($data[$key],105,40)));
   $adres2 = (string) rtrim(toUTF8(substr($data[$key],145,40)));
   $adres3 = (string) rtrim(toUTF8(substr($data[$key],185,40)));
   $poskod = (string) rtrim(toUTF8(substr($data[$key],225,6)));
   $mahalle = (string) rtrim(toUTF8(substr($data[$key],231,20)));
   $ilce = (string) rtrim(toUTF8(substr($data[$key],251,20)));
   $il = (string) rtrim(toUTF8(substr($data[$key],271,20)));
   $ulke = (string) rtrim(toUTF8(substr($data[$key],291,20)));
   $vda = (string) rtrim(toUTF8(substr($data[$key],311,30)));
   $vno = (string) rtrim(toUTF8(substr($data[$key],341,10)));
   $tcno = (string) rtrim(toUTF8(substr($data[$key],351,11)));
   $tsno = (string) rtrim(toUTF8(substr($data[$key],362,11)));
   $mersis = (string) rtrim(toUTF8(substr($data[$key],373,20)));
   $tur = (string) rtrim(toUTF8(substr($data[$key],393,3)));
   $kredi = floatVal(substr($data[$key],396,14) . '.' . substr($data[$key],410,4));
   $gun = floatVal(substr($data[$key],414,5));
   $iskonto1 = floatVal(substr($data[$key],419,4) . '.' . substr($data[$key],423,4));
   $iskonto2 = floatVal(substr($data[$key],427,4) . '.' . substr($data[$key],431,4));
   $plkod = floatVal(substr($data[$key],435,4));
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
