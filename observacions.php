<?php
// read JSon input
$data_back = json_decode(file_get_contents('php://input'));
 
// set json string to php variables
$fitxer = $data_back->{"fitxer"};
$usuari= $data_back->{"usuari"};
$lat = $data_back->{"lat"};
$lon= $data_back->{"lon"};
$id_feno= $data_back->{"id_feno"};
$descripcio = $data_back->{"descripcio"};
$tab = $data_back->{"tab"};

$dir = 'imatges';
if( !file_exists($dir) ) 
{
       $oldmask = umask(0);// helpful when used in linux server  
       mkdir ($dir, 0744);
}

$decoded_string = base64_decode($fitxer);
$image_name='prova.jpg';

file_put_contents ($dir.'/'.$image_name, $decoded_string);
 
echo $usuari,",",$lat,",",$lon,",",$id_feno,",",$descripcio,",",$tab,"\n",$fitxer;
?>