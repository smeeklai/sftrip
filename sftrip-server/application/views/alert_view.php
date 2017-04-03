<? header("Content-Type: text/html; charset=UTF-8"); ?>
<html>
	<head>
		
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
			<?php echo $map['js']; ?>

		<title><?php echo "SfTrip"; ?></title>
		<link rel='stylesheet' href="http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Sine/sms/css/test2_style.css">
	</head>
	<body>
		<div id="info">
			<div id="logo">
			<img id="logoBlack" src="http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Sine/sms/img/loogo.png" />
	      
		</div>
		<div id="relate">
			<h2 align="center">This map shows location of your relative :</h2>
			</div>
			<h1 align="center">Taxi Information</h1>
			
				<table border="0" id="taxiInfo" align="center">
			<tr>
				<td>License Plate:</td>
					<?php foreach($result as $row): ?>
				<td><textarea id="license" rows="1" cols="8" readonly><?php echo $row -> taxiPlate; ?></textarea></td>
				<?php endforeach; ?>
				
			</tr>
			<tr>
				<td align="right">
					
		<?php foreach($result as $row): ?>			
		<?php
 		if ($row -> licensePlatePicName  != '') { ?>
 			<img width="60%" height="30%" src="<?php echo "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/uploads/" . $row -> licensePlatePicName; ?>">  
    	<?php
		} else {
		?>
    		<img src="http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Sine/sms/img/noImageAvailable.jpg" width="40%" height="30%">
   		<?php
		}
		?>
 		<?php endforeach; ?>
 		</tr>
			</td>
			<tr>
				<td>Driver Number:</td>
						<?php foreach($result as $row): ?>
				<td><textarea rows="1" cols="8" readonly><?php echo $row -> taxiLicense; ?></textarea></h2></td>
			<?php endforeach; ?>
  
			</tr>
			<tr>
				<td align="right">
						
		<?php foreach($result as $row): ?>			
		<?php
 		if ($row -> driverNumberPicName  != '') { ?>
 			<img width="60%" height="30%" src="<?php echo "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/uploads/" . $row -> driverNumberPicName; ?>">  
    	<?php
		} else {
		?>
    		<img src="http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Sine/sms/img/noImageAvailable.jpg" width="40%" height="30%">
   		<?php
		}
	?>
 		<?php endforeach; ?>

			</td>
			<tr>
				<td>Taxi Color:</td>
					<?php foreach($result as $row): ?>
				<td><textarea id="license" rows="1" cols="8" readonly><?php echo $row -> taxiColor; ?></textarea></h2></td>
				<?php endforeach; ?>
			</tr>
			</tr>			
		</table>


		
	<img border="0" id="mean" align="center" src="http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Sine/sms/img/mean.png"> 
	
			</div>
			<div id="map_canvas">
					<?php echo $map['html']; ?>
			</div>
	
		</body>
		
</html>
