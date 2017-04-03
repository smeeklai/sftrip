<?php
class Trip_model extends CI_Model {
	function __construct() {
		parent::__construct();

		$this -> load -> helper('date');
	}

	function create_trip($email, $taxiPlate, $taxiLicense, $taxiColor, $startLat, $startLong, $desLat,
	$desLong, $lpPicName, $dnPicName) {
		$datestring = "%Y-%m-%d %H:%i:%s";
		$new_trip_data = array('email' => $email, 'startTime' => mdate($datestring), 'taxiPlate' => $taxiPlate,
		'taxiLicense' => $taxiLicense, 'taxiColor' => $taxiColor, 'startLat' => $startLat,
		'startLng' => $startLong, 'desLat' => $desLat, 'desLng' => $desLong, 
		'licensePlatePicName' => $lpPicName, 'driverNumberPicName' => $dnPicName);
		$this -> db -> insert('trip', $new_trip_data);
		$id = $this -> db -> insert_id();
		return $id;
	}

	function check_trip($id) {
		$this -> db -> where('tripID', $id);
		$query = $this -> db -> get('trip');
		if ($query -> num_rows == 1) {
			return true;
		}
	}

	function upload_picture($img) {
		$config['upload_path'] = './uploads/';
		//$config['file_name'] = 'my_image'.$_POST['imageType'];
		$config['allowed_types'] = 'gif|jpg|png';

		$this -> load -> library('upload', $config);
		$query = $this -> upload -> do_upload($img);
		if (!$query) {
			return $this -> upload -> display_errors();
		} else {
			return "success";
		}
	}

}
?>