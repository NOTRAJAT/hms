-- MySQL dump 10.13  Distrib 8.0.45, for macos26.2 (arm64)
--
-- Host: localhost    Database: hms
-- ------------------------------------------------------
-- Server version	9.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `billing_records`
--

DROP TABLE IF EXISTS `billing_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `billing_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `additional_fees` int NOT NULL,
  `bill_id` varchar(255) NOT NULL,
  `customer_name` varchar(255) NOT NULL,
  `customer_user_id` varchar(255) NOT NULL,
  `discounts` int NOT NULL,
  `issue_date` datetime(6) NOT NULL,
  `payment_status` varchar(255) NOT NULL,
  `room_charges` int NOT NULL,
  `service_charges` int NOT NULL,
  `service_items_json` varchar(5000) NOT NULL,
  `taxes` int NOT NULL,
  `total_amount` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKj1y4ky193w5a34phw0veqa1vc` (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `billing_records`
--

LOCK TABLES `billing_records` WRITE;
/*!40000 ALTER TABLE `billing_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `billing_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `adults` int NOT NULL,
  `base_price` int NOT NULL,
  `booking_id` varchar(255) NOT NULL,
  `check_in_date` date NOT NULL,
  `check_out_date` date NOT NULL,
  `children` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `customer_email` varchar(255) NOT NULL,
  `customer_mobile` varchar(255) NOT NULL,
  `customer_name` varchar(255) NOT NULL,
  `customer_user_id` varchar(255) NOT NULL,
  `gst_amount` int NOT NULL,
  `invoice_id` varchar(255) NOT NULL,
  `nights` int NOT NULL,
  `occupancy_adults` int NOT NULL,
  `occupancy_children` int NOT NULL,
  `payment_method` varchar(255) NOT NULL,
  `price_per_night` int NOT NULL,
  `room_code` varchar(255) NOT NULL,
  `room_type` varchar(255) NOT NULL,
  `service_charge_amount` int NOT NULL,
  `special_requests` varchar(1000) DEFAULT NULL,
  `status` enum('Cancelled','Confirmed') NOT NULL,
  `total_amount` int NOT NULL,
  `transaction_id` varchar(255) NOT NULL,
  `cancelled_at` datetime(6) DEFAULT NULL,
  `cancellation_note` varchar(500) DEFAULT NULL,
  `cancellation_refund_amount` int DEFAULT NULL,
  `payment_status` varchar(20) NOT NULL DEFAULT 'PAID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKnmi5a9y88gwia009jlw2xbosf` (`booking_id`),
  UNIQUE KEY `UKawdpwtjvu8di2pbww64e88xwv` (`transaction_id`),
  UNIQUE KEY `UKrnps32upy34b0x1xptc467r1b` (`invoice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `complaint_action_logs`
--

DROP TABLE IF EXISTS `complaint_action_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `complaint_action_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action_at` datetime(6) NOT NULL,
  `action_details` varchar(2000) DEFAULT NULL,
  `action_type` varchar(255) NOT NULL,
  `actor_user_id` varchar(255) NOT NULL,
  `assigned_department` varchar(255) DEFAULT NULL,
  `assigned_staff_member` varchar(255) DEFAULT NULL,
  `complaint_id` varchar(255) NOT NULL,
  `from_status` varchar(255) DEFAULT NULL,
  `to_status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `complaint_action_logs`
--

LOCK TABLES `complaint_action_logs` WRITE;
/*!40000 ALTER TABLE `complaint_action_logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `complaint_action_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `complaints`
--

DROP TABLE IF EXISTS `complaints`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `complaints` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `booking_id` varchar(255) NOT NULL,
  `category` varchar(255) NOT NULL,
  `complaint_id` varchar(255) NOT NULL,
  `contact_preference` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(500) NOT NULL,
  `expected_resolution_date` date DEFAULT NULL,
  `resolution_notes` varchar(1000) DEFAULT NULL,
  `status` enum('Closed','In_Progress','Open','Pending','Resolved') NOT NULL,
  `support_response` varchar(1000) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `assigned_department` varchar(255) DEFAULT NULL,
  `assigned_staff_member` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `escalated` bit(1) NOT NULL,
  `priority_level` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKeip4aylm0l3w5xeh35896wnli` (`complaint_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `complaints`
--

LOCK TABLES `complaints` WRITE;
/*!40000 ALTER TABLE `complaints` DISABLE KEYS */;
/*!40000 ALTER TABLE `complaints` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(512) NOT NULL,
  `email` varchar(255) NOT NULL,
  `failed_attempts` int NOT NULL,
  `locked` bit(1) NOT NULL,
  `mobile` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `admin` bit(1) NOT NULL,
  `password_change_required` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKt7jldpwa4qt2p21mox6lk8k9y` (`user_id`),
  UNIQUE KEY `UKrfbvkrffamfql7cjmen8v976v` (`email`),
  UNIQUE KEY `UKcuu4e61wdwoopdgsh61owqq2f` (`mobile`),
  UNIQUE KEY `UKbepynu3b6l8k2ppuq6b33xfxc` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,'Admin HQ','admin@hms.local',0,_binary '\0','+919111111111','System Admin','$2a$10$sXtutq3M5r7g.SV8WhvA3uJIRsRbfafe1x6ztztleT3EPj3VLjgsG','ADMIN-000001','admin',_binary '',_binary '\0');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rooms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `amenities_csv` varchar(1000) NOT NULL,
  `image_url` varchar(255) NOT NULL,
  `occupancy_adults` int NOT NULL,
  `occupancy_children` int NOT NULL,
  `price_per_night` int NOT NULL,
  `room_code` varchar(255) NOT NULL,
  `room_size_sq_ft` int NOT NULL,
  `room_type` varchar(255) NOT NULL,
  `bed_type` varchar(255) DEFAULT NULL,
  `room_status` varchar(255) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK82d187xrq1oo3hq0iod8tm1up` (`room_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rooms`
--

LOCK TABLES `rooms` WRITE;
/*!40000 ALTER TABLE `rooms` DISABLE KEYS */;
INSERT INTO `rooms` VALUES (1,_binary '','WiFi, TV, AC','/assets/Standard.png',2,1,6000,'STD-101',280,'Standard','Queen','AVAILABLE',NULL),(2,_binary '','WiFi, TV, AC, Mini-bar','/assets/Deluxe.png',3,2,8000,'DLX-210',360,'Deluxe','King','AVAILABLE',NULL),(3,_binary '','WiFi, TV, AC, Mini-bar, Balcony, Breakfast','/assets/Suite.png',4,2,14000,'STE-501',540,'Suite','King','AVAILABLE',NULL);
/*!40000 ALTER TABLE `rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staff_users`
--

DROP TABLE IF EXISTS `staff_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(512) NOT NULL,
  `department` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `failed_attempts` int NOT NULL,
  `locked` bit(1) NOT NULL,
  `mobile` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password_change_required` bit(1) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK69s84i1hvm8dc3mej62hw4rrc` (`user_id`),
  UNIQUE KEY `UK84ntv1iab9fa9byg67plxw9fb` (`username`),
  UNIQUE KEY `UKcg5gp3sunabfoh3rwkqml3xmg` (`email`),
  UNIQUE KEY `UKdorjyp17ptt4feda7n54ln7pi` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff_users`
--

LOCK TABLES `staff_users` WRITE;
/*!40000 ALTER TABLE `staff_users` DISABLE KEYS */;
/*!40000 ALTER TABLE `staff_users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-22 19:55:38
