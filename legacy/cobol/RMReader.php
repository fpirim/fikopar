<?php
//$data = RMReader::read('DATA\\SAYIM.DAT');
//echo 'bitti';
//var_dump($data);
//fgets(STDIN);

class RMReader {
    private static $headers = Array(
        "Signature"         => 7,
        "MinRecord"         => 17,
        "MaxRecord"         => 19,
        "SpaceCode"         => 22,
        "NumberCode"        => 23,
        "KeyCompression"    => 24,
        "Compression"       => 24,
        "KeyNumberCode"     => 25,
        "BlockSize"         => 27,
        "BlockIncrement"    => 29,
        "BlockContains"     => 31,
        "IndexBlocks"       => 45,
        "NoRecords"         => 53,
        "IntegrityFlag"     => 55,
        "AltKeyLength"      => 257,
        "PriKeyOffSet"      => 257,
        "AllocationInc"     => 257,
        "AltKeyOffset"      => 259,
        "PriKeyLength"      => 259,
        "EmptyBlocks"       => 261
    );
    private static $data = NULL;
    private static $countRecord = 0;
    private static $lGotEnough = false;
    private static $fOffset = 0;
    private static $fBlockSize = 0;
    
    public static function read($fileName, $filter=NULL) {
    
        self::$data = file_get_contents($fileName);
        self::$countRecord = 0;
        self::$lGotEnough = false;
        self::$fBlockSize = self::getShort(self::$headers['BlockSize']);
        $finishOffset = fileSize($fileName) / self::$fBlockSize;
        $primaryKeyLength = self::getShort(self::$headers['PriKeyLength']);
        self::$fOffset = 0;
        self::checkEnough();
        $dataArr = Array();
        while (self::$fOffset <  $finishOffset) // fOffset*fBlockSize<fileSize)
        {
            self::checkEnough();
            //$flag = false;
            //echo getBlockID() ."\n";
            if (self::isValidBlockID()) {
                $nStartPos = 11;
                $nBytesLeft = self::getCharactersFollowing() - 11;
                $nSkip = 0;
                while (self::getShort(self::getPosition() + $nStartPos) == 0) {
                    $nSkip += 4;
                    $nStartPos += 4;
                    $nBytesLeft -= 4;
                }
                while ($nBytesLeft > 0) {
                    $nSize = self::getShort($nStartPos + self::getPosition());
                    $buf = substr(self::$data, $nStartPos + self::getPosition() + 1, $nSize);
                    $nBytesLeft -= $nSize + $nSkip + 4;
                    $nStartPos += $nSize + $nSkip + 4;
                    $decodedString = self::deCompress($buf);
                    if($filter==NULL or self::startsWith($decodedString, $filter)) {
                        $dataArr[rtrim(substr($decodedString, 0, $primaryKeyLength))] = $decodedString;
                        self::$countRecord++;
                    }
                }
            }
            /*
            if (isValidKeyID()) {
                $flag = true;
            }
            if (!$flag) {
                //System.out.println(fOffset + " - " + getBlockID());
            }*/
            self::$fOffset++;
        }
        self::$data = NULL;
        return $dataArr;
    }
    private static function startsWith($haystack, $needle) {
        return substr($haystack, 0, strlen($needle)) === $needle;
    }

    private static function deCompress($gbuf) {
        $buf = unpack('C*', $gbuf);
        $cOutPut = '';
        $pIn = 1;
        $nCount = count($buf);
        $nMax = 0;
        $lDoIt = true;
        $fill_char = chr(0);
        $fill_num = 0; $i = 0;
        while ($nCount > 0) {
			$nMax = $buf[$pIn] & 0xFF;
            $lDoIt = true;
			if ($nMax > 127) {
				if ($nMax > 231) {
					$pIn++;
					$nCount--;
					$fill_char = chr($buf[$pIn] & 0xFF);
					$fill_num = $nMax - 229;
				} // else if (nMax > 207) {
				// fill_char = (char) 0;
				// fill_num = nMax - 210;
				// }
				else if ($nMax > 191) {
					$fill_char = '0';
					$fill_num = $nMax - 190;
				} else {
					$fill_char = ' ';
					$fill_num = $nMax - 126;
				}
			} else { 
				$lDoIt = false;
				for ($i = 1; $i <= $nMax; $i++) {
					$pIn++;
					$nCount--;
					
                    $cOutPut .= chr($buf[$pIn] & 0xFF);
					//cOutPut += (char) (buf.get(pIn) & 0xFF);
					
				}
			}
			if ($lDoIt)
				for ($i = 1; $i <= $fill_num; $i++)
					$cOutPut .= $fill_char;
					//cOutPut += fill_char;
			$pIn++;
			$nCount--;
		}
		//return new String(cOutPut.toByteArray(), "ISO-8859-9");
		//return new String(cOutPut.toByteArray(), "cp857");
        return $cOutPut;
    }

    private static function getBlockID() {
        return self::$lGotEnough ? self::getByte(self::getPosition() + 2) : 0;
    }

    private static function getPosition() {
        return self::$fOffset * self::$fBlockSize;
    }
    
    private static function getByte($fOffset) {
        $val = unpack('C1', substr(self::$data, $fOffset - 1,1) );
        return $val[1];
    }

    private static function getShort($fOffset) {
        $val = unpack('n1', substr(self::$data, $fOffset - 1,2) );
        return $val[1];
    }
    
    private static function isValidKeyID() {
        $blockId = self::getBlockID();
        $value = ($blockId == 5);
        return !$value && $blockId == 7 ? true : $value;
    }

    private static function isValidBlockID() {
        $blockId = self::getBlockID();
        $value = ($blockId == 6);
        return !$value && $blockId == 7 ? true : $value;
    }

    private static function getCharactersFollowing() {
        return self::$lGotEnough ? self::getShort(self::getPosition() + 7) : 0;
    }

    private static function checkEnough() {
        return self::$lGotEnough = self::$fBlockSize > 0 ? true : false;
    }
}

?>