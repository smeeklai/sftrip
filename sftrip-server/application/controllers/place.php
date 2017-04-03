<?php
class Place extends CI_Controller {
	public function __construct() {
		parent::__construct();
		/*
		 load you helper library
		 */
		/*
		 load you model
		 */
		$this -> load -> model('place_model');
	}

	function index() {
		$email = $this -> input -> post("email");
		$placeID = $this -> input -> post("placeID");
		$placeName = $this -> input -> post("placeName");
		$lat = $this -> input -> post("latitude");
		$long = $this -> input -> post("longtitude");

		if ($this -> place_model -> is_enable_to_insert($email, $placeID) == false) {
			$arr['success'] = "0";
			$arr['error'] = "1";
			$arr['error_msg'] = "The " . $placeID . " already had information. Do you want to overwrite the data?";
		} else {
			$this -> place_model -> add_favorite_place($email, $placeID, $placeName, $lat, $long);
			$query = $this -> place_model -> check_favorite_place($email, $placeID);
			if ($query) {
				$arr['success'] = "1";
				$arr['error'] = "0";
				$arr['error_msg'] = "";
			} else {
				$arr['success'] = "0";
				$arr['error'] = "1";
				$arr['error_msg'] = "Something wrong with the transaction. Please try again";
			}
		}
		echo json_encode($arr);
	}
	
	function updateFavorPlace() {
		$email = $this -> input -> post("email");
		$placeID = $this -> input -> post("placeID");
		$placeName = $this -> input -> post("placeName");
		$lat = $this -> input -> post("latitude");
		$long = $this -> input -> post("longtitude");
		
		$query = $this -> place_model -> update_favorite_place($email, $placeID, $placeName, $lat, $long);
			if ($query) {
				$arr['success'] = "1";
				$arr['error'] = "0";
				$arr['error_msg'] = "";
			} else {
				$arr['success'] = "0";
				$arr['error'] = "1";
				$arr['error_msg'] = "Something wrong with the transaction. Please try again";
			}
		echo json_encode($arr);
	}

	function getLatitude() {
		$email = $this -> input -> post("email");
		$placeID = $this -> input -> post("placeID");
		$query = $this -> place_model -> getLatitude($placeID, $email);
		$arr['latitude'] = $query -> result();
		echo json_encode($arr);
	}

	function getLongtitude() {
		$email = $this -> input -> post("email");
		$placeID = $this -> input -> post("placeID");
		$query = $this -> place_model -> getLongtitude($placeID, $email);
		$arr['longtitude'] = $query -> result();
		echo json_encode($arr);
	}

	function getInformation() {
		$email = $this -> input -> post("email");
		$placeID = $this -> input -> post("placeID");
		$query = $this -> place_model -> getInformation($email, $placeID);
		if ($query == null) {
			$arr['success'] = "0";
			$arr['error'] = "1";
			$arr['error_msg'] = "You must add this favorite place before using";
		} else {
			foreach ($query as $row) {
				$arr['success'] = "1";
				$arr['placeName'] = $row -> placeName;
				$arr['latitude'] = $row -> latitude;
				$arr['longtitude'] = $row -> longtitude;
			}
		}
		echo json_encode($arr);
	}

}
?>