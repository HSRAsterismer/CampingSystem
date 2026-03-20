package com.camping.system.config;

import com.camping.system.entity.CampingSite;
import com.camping.system.entity.FraudEvent;
import com.camping.system.entity.ObservationRecord;
import com.camping.system.entity.PaymentOrder;
import com.camping.system.entity.Reservation;
import com.camping.system.entity.User;
import com.camping.system.enums.ObservationStatus;
import com.camping.system.enums.PaymentChannel;
import com.camping.system.enums.PaymentStatus;
import com.camping.system.enums.ReservationStatus;
import com.camping.system.enums.RiskLevel;
import com.camping.system.enums.SiteStatus;
import com.camping.system.enums.UserRole;
import com.camping.system.repository.CampingSiteRepository;
import com.camping.system.repository.FraudEventRepository;
import com.camping.system.repository.ObservationRecordRepository;
import com.camping.system.repository.PaymentOrderRepository;
import com.camping.system.repository.ReservationRepository;
import com.camping.system.repository.UserRepository;
import com.camping.system.util.PasswordUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CampingSiteRepository campingSiteRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final ObservationRecordRepository observationRecordRepository;
    private final FraudEventRepository fraudEventRepository;

    public DatabaseSeeder(UserRepository userRepository,
                          CampingSiteRepository campingSiteRepository,
                          ReservationRepository reservationRepository,
                          PaymentOrderRepository paymentOrderRepository,
                          ObservationRecordRepository observationRecordRepository,
                          FraudEventRepository fraudEventRepository) {
        this.userRepository = userRepository;
        this.campingSiteRepository = campingSiteRepository;
        this.reservationRepository = reservationRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.observationRecordRepository = observationRecordRepository;
        this.fraudEventRepository = fraudEventRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = createUser("admin", "admin123", "系统管理员", "13800138000", UserRole.ADMIN);
        User camper = createUser("camper", "camper123", "星野营地主理人", "13800138001", UserRole.USER);
        User observer = createUser("eco", "eco123", "生态观察员", "13800138002", UserRole.OBSERVER);

        CampingSite site1 = createSite("CAMP-001", "祁连星河营地", "甘肃省", "张掖市", "丹霞地貌观景带北侧", 38.972510, 100.448120, 18, 268, SiteStatus.OPEN, 5, 92, "观星,徒步,供电,应急医疗", "高海拔,星空,摄影", "以星空观测和低干扰营地管理著称的旗舰露营点");
        CampingSite site2 = createSite("CAMP-002", "雨林溪谷营地", "云南省", "西双版纳州", "勐腊县热带雨林生态廊道", 21.932410, 101.263560, 14, 328, SiteStatus.OPEN, 5, 95, "科普讲解,昆虫灯诱,生态巡护", "雨林,观鸟,水系", "侧重生物多样性观察与微栖息地记录");
        CampingSite site3 = createSite("CAMP-003", "海岸风语营地", "福建省", "宁德市", "霞浦东冲半岛生态海岸", 26.914200, 120.022400, 12, 298, SiteStatus.OPEN, 4, 88, "冲洗区,露台,电源补给", "海岸,日出,海鸟", "适合海岸带生态观察与轻量化预订体验");
        CampingSite site4 = createSite("CAMP-004", "雪峰松林营地", "四川省", "阿坝州", "四姑娘山双桥沟保护缓冲区", 31.130500, 102.886400, 10, 368, SiteStatus.MAINTENANCE, 5, 90, "防寒补给,巡护联动,高山告警", "雪山,松林,高海拔", "用于高山生态体验与科考配套，当前维护中");
        CampingSite site5 = createSite("CAMP-005", "湖湾晨雾营地", "浙江省", "湖州市", "安吉天荒坪湖湾步道", 30.638200, 119.675900, 16, 238, SiteStatus.OPEN, 4, 86, "亲子观察,夜巡,基础餐饮", "湖湾,湿地,亲子", "兼顾家庭露营与湿地鸟类晨间观察");

        Reservation reservation1 = createReservation(camper, site1, "RSV20260311001", LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 4, 2, new BigDecimal("1072.00"), ReservationStatus.CONFIRMED, RiskLevel.LOW, "", "127.0.0.1", "观星摄影预订", LocalDateTime.now().minusDays(2));
        Reservation reservation2 = createReservation(camper, site2, "RSV20260312002", LocalDate.now().plusDays(5), LocalDate.now().plusDays(7), 6, 3, new BigDecimal("1968.00"), ReservationStatus.PENDING_PAYMENT, RiskLevel.MEDIUM, "weekend_peak,multi_tent", "127.0.0.1", "团队生态观察活动", LocalDateTime.now().minusDays(1));
        Reservation reservation3 = createReservation(observer, site3, "RSV20260314003", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), 2, 1, new BigDecimal("298.00"), ReservationStatus.CONFIRMED, RiskLevel.LOW, "", "127.0.0.8", "海鸟观察单晚预订", LocalDateTime.now().minusDays(3));

        createPayment(reservation1, camper, "PAY20260311001", PaymentChannel.WECHAT, new BigDecimal("1072.00"), PaymentStatus.SETTLED, "TXN20260311001", LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), "系统管理员", "批量自动结算");
        createPayment(reservation3, observer, "PAY20260314003", PaymentChannel.ALIPAY, new BigDecimal("298.00"), PaymentStatus.PAID, "TXN20260314003", LocalDateTime.now().minusDays(3), null, "生态观察员", null);

        createObservation(site1, observer, "黑鹳", "鸟类", 2, "晴", LocalDateTime.now().minusDays(1).withHour(6), "栖息于岩壁热流区域", "稀有", 93, "迁徙路径稳定，建议保持低噪音巡护", ObservationStatus.VERIFIED);
        createObservation(site2, observer, "中华鬣羚", "兽类", 1, "多云", LocalDateTime.now().minusDays(2).withHour(8), "林下灌丛", "珍稀", 96, "发现新鲜蹄印，需继续监测活动轨迹", ObservationStatus.VERIFIED);
        createObservation(site5, camper, "白鹭", "鸟类", 6, "晨雾", LocalDateTime.now().minusHours(10), "湖湾浅滩", "常见", 82, "清晨集群觅食，湿地水位正常", ObservationStatus.SUBMITTED);
        createObservation(site3, observer, "滨鹬", "鸟类", 12, "晴", LocalDateTime.now().minusDays(4).withHour(5), "潮间带", "常见", 85, "数量波动与潮汐相关", ObservationStatus.VERIFIED);

        createFraud(camper.getId(), reservation2.getId(), "RESERVATION_REVIEW", RiskLevel.MEDIUM, "REVIEW", "127.0.0.1", "周末高峰+多帐篷组合，系统已标记重点监测", LocalDateTime.now().minusHours(18));
        createFraud(camper.getId(), null, "RESERVATION_BLOCK", RiskLevel.HIGH, "BLOCK", "127.0.0.1", "短时间内重复提交相似预订请求，已被拦截", LocalDateTime.now().minusHours(12));
        createFraud(observer.getId(), null, "PAYMENT_RECHECK", RiskLevel.MEDIUM, "REVIEW", "127.0.0.8", "支付设备切换频繁，建议人工复核", LocalDateTime.now().minusHours(8));
    }

    private User createUser(String username, String password, String displayName, String phone, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtils.hash(username, password));
        user.setDisplayName(displayName);
        user.setPhone(phone);
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private CampingSite createSite(String code,
                                   String name,
                                   String province,
                                   String city,
                                   String address,
                                   double latitude,
                                   double longitude,
                                   int capacity,
                                   int basePrice,
                                   SiteStatus status,
                                   int scenicLevel,
                                   int ecoIndex,
                                   String facilities,
                                   String tags,
                                   String description) {
        CampingSite site = new CampingSite();
        site.setCode(code);
        site.setName(name);
        site.setProvince(province);
        site.setCity(city);
        site.setAddress(address);
        site.setLatitude(BigDecimal.valueOf(latitude));
        site.setLongitude(BigDecimal.valueOf(longitude));
        site.setCapacity(capacity);
        site.setBasePrice(BigDecimal.valueOf(basePrice));
        site.setStatus(status);
        site.setScenicLevel(scenicLevel);
        site.setEcoIndex(ecoIndex);
        site.setFacilities(facilities);
        site.setTags(tags);
        site.setDescription(description);
        return campingSiteRepository.save(site);
    }

    private Reservation createReservation(User user,
                                          CampingSite site,
                                          String reservationNo,
                                          LocalDate startDate,
                                          LocalDate endDate,
                                          int guestCount,
                                          int tentCount,
                                          BigDecimal totalAmount,
                                          ReservationStatus status,
                                          RiskLevel riskLevel,
                                          String riskTags,
                                          String sourceIp,
                                          String remark,
                                          LocalDateTime createdAt) {
        Reservation reservation = new Reservation();
        reservation.setReservationNo(reservationNo);
        reservation.setUserId(user.getId());
        reservation.setSiteId(site.getId());
        reservation.setContactName(user.getDisplayName());
        reservation.setContactPhone(user.getPhone());
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setGuestCount(guestCount);
        reservation.setTentCount(tentCount);
        reservation.setTotalAmount(totalAmount);
        reservation.setStatus(status);
        reservation.setRiskLevel(riskLevel);
        reservation.setRiskTags(riskTags);
        reservation.setSourceIp(sourceIp);
        reservation.setRemark(remark);
        if (status == ReservationStatus.CONFIRMED) {
            reservation.setPaidAt(createdAt.plusHours(1));
        }
        reservation = reservationRepository.save(reservation);
        reservation.setCreatedAt(createdAt);
        reservation.setUpdatedAt(createdAt.plusHours(2));
        return reservationRepository.save(reservation);
    }

    private void createPayment(Reservation reservation,
                               User user,
                               String orderNo,
                               PaymentChannel channel,
                               BigDecimal amount,
                               PaymentStatus status,
                               String transactionNo,
                               LocalDateTime paidAt,
                               LocalDateTime settledAt,
                               String operatorName,
                               String note) {
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setOrderNo(orderNo);
        paymentOrder.setReservationId(reservation.getId());
        paymentOrder.setUserId(user.getId());
        paymentOrder.setChannel(channel);
        paymentOrder.setAmount(amount);
        paymentOrder.setStatus(status);
        paymentOrder.setTransactionNo(transactionNo);
        paymentOrder.setPaidAt(paidAt);
        paymentOrder.setSettledAt(settledAt);
        paymentOrder.setOperatorName(operatorName);
        paymentOrder.setSettlementNote(note);
        paymentOrder = paymentOrderRepository.save(paymentOrder);
        paymentOrder.setCreatedAt(paidAt.minusMinutes(10));
        paymentOrder.setUpdatedAt(settledAt == null ? paidAt : settledAt);
        paymentOrderRepository.save(paymentOrder);
    }

    private void createObservation(CampingSite site,
                                   User observer,
                                   String speciesName,
                                   String category,
                                   int quantity,
                                   String weather,
                                   LocalDateTime observationTime,
                                   String habitat,
                                   String rarityLevel,
                                   int environmentalScore,
                                   String notes,
                                   ObservationStatus status) {
        ObservationRecord record = new ObservationRecord();
        record.setSiteId(site.getId());
        record.setObserverId(observer.getId());
        record.setSpeciesName(speciesName);
        record.setCategory(category);
        record.setQuantity(quantity);
        record.setWeather(weather);
        record.setObservationTime(observationTime);
        record.setCoordinates(site.getLatitude() + "," + site.getLongitude());
        record.setHabitat(habitat);
        record.setRarityLevel(rarityLevel);
        record.setEnvironmentalScore(environmentalScore);
        record.setNotes(notes);
        record.setStatus(status);
        record = observationRecordRepository.save(record);
        record.setCreatedAt(observationTime.plusMinutes(15));
        record.setUpdatedAt(observationTime.plusMinutes(20));
        observationRecordRepository.save(record);
    }

    private void createFraud(Long userId,
                             Long reservationId,
                             String eventType,
                             RiskLevel riskLevel,
                             String actionTaken,
                             String sourceIp,
                             String detail,
                             LocalDateTime createdAt) {
        FraudEvent event = new FraudEvent();
        event.setUserId(userId);
        event.setReservationId(reservationId);
        event.setEventType(eventType);
        event.setRiskLevel(riskLevel);
        event.setActionTaken(actionTaken);
        event.setSourceIp(sourceIp);
        event.setDetail(detail);
        event = fraudEventRepository.save(event);
        event.setCreatedAt(createdAt);
        event.setUpdatedAt(createdAt.plusMinutes(5));
        fraudEventRepository.save(event);
    }
}
