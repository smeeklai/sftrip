<?php
class LogInfo extends CI_Controller {

	public function __construct() {
		parent::__construct();
		/*
		 load you helper library
		 */
		/*
		 load you model
		 */
		$this -> load -> model('loginfo_model');
	}

	public function index() {
		$tripID = (int)$this -> input -> post("tripID");
		$lat = $this -> input -> post("latitude");
		$lng = $this -> input -> post("longtitude");
		//$tripID = 91;
		//$lat = 13.6536115;
		//$lng = 100.4977351;
		$passengerStatus = "";
		$currentRemainingDistance = $this -> calculateRemainingDistance($tripID, $lat, $lng);
		$status = $this -> didUserArrive($currentRemainingDistance);
		$indexOfCRD = $this -> loginfo_model -> getIndexOfCRD($tripID);
		$closetRemainingDistance = (double)$this -> loginfo_model -> getClosetRemainingDistance($indexOfCRD);
		if ($indexOfCRD == null) {
			$currentID = $this -> loginfo_model -> create_logInfo($tripID, $lat, $lng, $currentRemainingDistance, 1, $status);
			$passengerStatus = "normal";
		} else {
			if ((double)$currentRemainingDistance < $closetRemainingDistance) {
				$this -> loginfo_model -> setCurrentCRDPToOld($indexOfCRD);
				$currentID = $this -> loginfo_model -> create_logInfo($tripID, $lat, $lng, $currentRemainingDistance, 1, $status);
				$passengerStatus = "normal";
			} else {
				$currentID = $this -> loginfo_model -> create_logInfo($tripID, $lat, $lng, $currentRemainingDistance, 0, $status);
				$passengerStatus = $this -> identifyPassengerStatus((double)$currentRemainingDistance,
				$closetRemainingDistance, $tripID);
			}
		}

		$query2 = $this -> loginfo_model -> check_logInfo($tripID, $currentID);
		if ($query2) {
			$arr['success'] = "1";
			$arr['error'] = "0";
			$arr['error_msg'] = "";
			$arr['arrived'] = $status;
			$arr['RD'] = $currentRemainingDistance;
			$arr['cID'] = $currentID;
			$arr['indexOfCRD'] = $indexOfCRD;
			$arr['CRD'] = $closetRemainingDistance;
			$arr['passengerStatus'] = $passengerStatus;
		} else {
			$arr['success'] = "0";
			$arr['error'] = "1";
			$arr['error_msg'] = "The logInfo could not stored in database successfully";
			$arr['arrived'] = 0;
		}
		echo json_encode($arr);
	}

	function calculateRemainingDistance($tripID, $currentLat, $currentLng) {
		$RADIUS = 6371;
		$desLat = (double)$this -> loginfo_model -> getDestinationLatitude($tripID);
		$desLng = (double)$this -> loginfo_model -> getDestinationLongtitude($tripID);
		$lat1 = deg2rad($currentLat);
		$lat2 = deg2rad($desLat);
		$dLat = deg2rad($desLat - $currentLat);
		$dLng = deg2rad($desLng - $currentLng);
		//for optimization, using octave or mathlab or python3 to optimize the result accuracy 
		$a = sin($dLat / 2) * sin($dLat / 2) + sin($dLng / 2) * sin($dLng / 2) * cos($lat1) * cos($lat2);
		$c = 2 * atan2(sqrt($a), sqrt(1 - $a));
		$d = number_format((($RADIUS * $c) * 1000), 2, '.', '');
		$distance = (float)$d;
		return $distance;
	}

	function didUserArrive($remainingDistance) {
		if ($remainingDistance <= 200) {
			return 1;
		} else if ($remainingDistance > 200) {
			return 0;
		} else {
			return 9;
		}
	}

	function identifyPassengerStatus($currentRD, $closetRD, $tripID) {
		if ($closetRD < 3000) {
			$APD1 = 600;
			$APD2 = 2000;
		} else {
			$APD1 = $closetRD * 0.2;
			$APD2 = $closetRD * 0.5;
		}
		if ($this->doesUserMove($currentRD, $tripID) == TRUE) {
			if ($currentRD - $closetRD < $APD1) {
				return "normal";
			} else if ($currentRD - $closetRD < $APD2) {
				return "suspicious";
			} else {
				return "dangerous";
			}
		} else {
			return "not moving";
		}

	}

	function doesUserMove($currentRD, $tripID) {
		$last_three_rd = $this -> loginfo_model -> getRemainingDistance($tripID);
		if ($last_three_rd != null) {
			$check_value = 0;
			if (abs($last_three_rd[0] - $currentRD  < 100)) {
				$check_value += 1;
			}
			if (abs($last_three_rd[1] - $currentRD) < 100) {
				$check_value += 1;
			}
			if (abs($last_three_rd[2] - $currentRD) < 100) {
				$check_value += 1;
			}
			if ($check_value == 3) {
				return FALSE;
			} else {
				return TRUE;
			}
		} else {
			return TRUE;
		}
	}
	
	function changeCRD() {
		$tripID = (int)$this -> input -> post("tripID");
		//$tripID = 325;
		$indexOfCRD = $this -> loginfo_model -> getIndexOfCRD($tripID);
		$lastestIndex = $this->loginfo_model->getIndexOfLastRow($tripID);
		$this -> loginfo_model -> setCurrentCRDPToOld($indexOfCRD);
		$query = $this-> loginfo_model->setRowToBeCRD($lastestIndex);
		
		if($query) {
			$arr['msg'] = "successfully changed CRD"; 
		} else {
			$arr['msg'] = "Unsuccessfully changed CRD. Please try again";
		}
		echo json_encode($arr);	
	}

	/**public function calculateRemainingDistance() {
	 $RADIUS = 6371;
	 $desLat = 13.650373831842236;
	 $desLng = 100.49441065639257;
	 $lat1 = deg2rad(13.652353745870842);
	 $lat2 = deg2rad($desLat);
	 $dLat = deg2rad($desLat - 13.652353745870842);
	 $dLng = deg2rad($desLng - 100.49481868743896);

	 $a = sin($dLat/2) * sin($dLat/2) + sin($dLng/2) * sin($dLng/2) * cos($lat1) * cos($lat2);
	 $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
	 $d = number_format((($RADIUS * $c) * 1000), 2, '.', '');
	 $distance = (float)$d;
	 return $distance;
	 }*/
}
?>