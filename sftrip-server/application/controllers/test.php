<?php
class Test extends CI_Controller {
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

	function index() {
		$test = "ลอง";
		//$this -> login_model -> create_member($test, $test, $test, $test, $test, $test, $test, $test);
	}
	
	public function calculateRemainingDistance() {
		$RADIUS = 6371;
		$desLat = 13.651041732952208;
		$desLng = 100.49670025706291;
		$lat1 = deg2rad(13.6515847);
		$lat2 = deg2rad($desLat);
		$dLat = deg2rad($desLat - 13.6515847);
		$dLng = deg2rad($desLng - 100.49382);
		
		$a = sin($dLat/2) * sin($dLat/2) + sin($dLng/2) * sin($dLng/2) * cos($lat1) * cos($lat2);
		$c = 2 * atan2(sqrt($a), sqrt(1 - $a));
		$d = number_format((($RADIUS * $c) * 1000), 2, '.', '');
		$distance = (float)$d;
		echo $distance;
	}
	
	public function test(){
		$result = $this->loginfo_model->getLastestRemainingDistance(105);
		echo $result;
		
	}

}
?>
