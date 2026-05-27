package BUS;

import DAO.DAO_TaiKhoan;
import Entity.TaiKhoan;
import Utils.PasswordUtils;
import java.util.List;

/**
 * BUS_TaiKhoan - Lớp Business Logic xử lý nghiệp vụ liên quan đến Tài Khoản.
 * Tầng BUS nằm giữa giao diện (GUI) và tầng truy xuất DB (DAO). Mọi thao tác
 * với tài khoản đều đi qua đây trước khi xuống DAO.
 */
public class BUS_TaiKhoan {

	// Đối tượng DAO để giao tiếp với database
	private DAO_TaiKhoan daoTaiKhoan;

	/**
	 * Constructor: Khởi tạo đối tượng DAO khi tạo BUS
	 */
	public BUS_TaiKhoan() {
		this.daoTaiKhoan = new DAO_TaiKhoan();
	}

	// =========================================================
	// 1. ĐĂNG NHẬP - có tích hợp tự động băm mật khẩu thô cũ
	// =========================================================
	/**
	 * Xác thực đăng nhập của người dùng. Đặc biệt: Nếu DB đang lưu mật khẩu thô
	 * (chưa băm từ hệ thống cũ), hàm sẽ tự động băm và cập nhật lại DB ngay lần đầu
	 * đăng nhập đúng.
	 *
	 * @param tenDangNhap    Tên đăng nhập người dùng nhập vào
	 * @param matKhauNhapVao Mật khẩu người dùng nhập vào (dạng thô)
	 * @return true nếu đăng nhập thành công, false nếu thất bại
	 */
	public boolean authenticate(String tenDangNhap, String matKhauNhapVao) {
		// Truy vấn DB lấy thông tin tài khoản theo tên đăng nhập
		TaiKhoan tk = daoTaiKhoan.getTaiKhoan(tenDangNhap);

		// Nếu không tìm thấy tài khoản hoặc mật khẩu trong DB bị null -> từ chối
		if (tk == null || tk.getMatKhau() == null) {
			return false;
		}

		// Kiểm tra trạng thái làm việc của nhân viên gắn với tài khoản
		// Nếu nhân viên đã nghỉ việc hoặc bị ẩn -> không cho đăng nhập
		if (tk.getNhanVienId() != null && tk.getNhanVienId().getTrangThaiLamViec() != null) {
			String trangThai = tk.getNhanVienId().getTrangThaiLamViec().name();
			if ("NGHI_VIEC".equalsIgnoreCase(trangThai) || "AN".equalsIgnoreCase(trangThai)) {
				System.out.println("Đăng nhập thất bại: Nhân viên này đã nghỉ việc!");
				return false;
			}
		}

		String matKhauDB = tk.getMatKhau(); // Lấy mật khẩu đang lưu trong DB ra

		// Nhận dạng mật khẩu thô: Chuỗi bcrypt hợp lệ luôn chứa dấu ":"
		// Nếu KHÔNG có ":" -> đây là mật khẩu thô từ hệ thống cũ, chưa được băm
		boolean laMKTho = !matKhauDB.contains(":");

		if (laMKTho) {
			// So sánh trực tiếp mật khẩu thô trong DB với mật khẩu người dùng vừa nhập
			if (!matKhauDB.equals(matKhauNhapVao)) {
				return false; // Sai mật khẩu
			}

			// Nếu đúng -> tiến hành băm mật khẩu thô để bảo mật hơn
			String mkBam = PasswordUtils.hashPassword(matKhauNhapVao);

			// Lấy mã nhân viên để dùng làm điều kiện cập nhật DB
			String idNV = tk.getNhanVienId() != null ? tk.getNhanVienId().getNhanVien() : "";

			// Ghi đè mật khẩu đã băm lên mật khẩu thô trong DB (migration tự động)
			daoTaiKhoan.capNhatTaiKhoanTheoMaNV(idNV, tk.getTenDangNhap(), mkBam);
			return true; // Đăng nhập thành công
		}

		// Trường hợp bình thường: mật khẩu trong DB đã là bcrypt hash
		// Dùng hàm xác thực constant-time (chống timing attack) để so sánh
		return PasswordUtils.verifyPassword(matKhauNhapVao, matKhauDB);
	}

	// =========================================================
	// 2. THÊM TÀI KHOẢN MỚI (từ màn hình quản lý)
	// =========================================================
	/**
	 * Thêm một tài khoản mới vào hệ thống. Tự động băm mật khẩu thô trước khi lưu
	 * xuống DB.
	 *
	 * @param tk Đối tượng TaiKhoan cần thêm (mật khẩu vẫn còn dạng thô)
	 * @return true nếu thêm thành công, false nếu trùng tên hoặc mật khẩu không hợp
	 *         lệ
	 */
	public boolean themTaiKhoanMoi(TaiKhoan tk) {
		// Kiểm tra tên đăng nhập đã tồn tại trong DB chưa
		if (daoTaiKhoan.checkTrungTenDangNhap(tk.getTenDangNhap())) {
			return false; // Trùng tên đăng nhập -> không cho thêm
		}

		String matKhauTho = tk.getMatKhau();

		// Kiểm tra mật khẩu có đủ độ mạnh không (tối thiểu 8 ký tự, có chữ và số)
		if (validateMatKhauMoi(matKhauTho)) {
			tk.setMatKhau(PasswordUtils.hashPassword(matKhauTho)); // Băm trước khi lưu
			return daoTaiKhoan.themTaiKhoan(tk); // Gọi DAO thêm vào DB
		}
		return false; // Mật khẩu yếu -> từ chối
	}

	// =========================================================
	// 3. CẬP NHẬT TÀI KHOẢN (từ màn hình quản lý)
	// =========================================================
	/**
	 * Cập nhật thông tin tài khoản (tên đăng nhập, mật khẩu) theo mã nhân viên. Hỗ
	 * trợ 3 tình huống: - Quản lý để trống ô mật khẩu: giữ nguyên mật khẩu cũ -
	 * Quản lý nhập mật khẩu mới thô: băm rồi lưu - Mật khẩu đã là bcrypt (bắt đầu
	 * bằng $2, dài ≥ 60 ký tự): lưu thẳng
	 *
	 * @param maNV           Mã nhân viên dùng để tìm tài khoản
	 * @param tenDangNhap    Tên đăng nhập mới
	 * @param matKhauNhapVao Mật khẩu mới (có thể để trống hoặc thô hoặc đã hash)
	 * @return true nếu cập nhật thành công
	 */
	public boolean capNhatTaiKhoanTheoMaNV(String maNV, String tenDangNhap, String matKhauNhapVao) {
		String matKhauLuuDB = matKhauNhapVao; // Mặc định lấy giá trị người dùng nhập

		if (matKhauNhapVao == null || matKhauNhapVao.isEmpty()) {
			// Quản lý không nhập mật khẩu -> lấy mật khẩu cũ từ DB để giữ nguyên
			TaiKhoan tkCu = daoTaiKhoan.layTaiKhoanTheoMaNV(maNV);
			if (tkCu != null) {
				matKhauLuuDB = tkCu.getMatKhau(); // Giữ nguyên hash cũ
			}
		}
		// Kiểm tra mật khẩu có phải bcrypt chưa (bcrypt bắt đầu bằng "$2" và dài ≥ 60
		// ký tự)
		else if (!matKhauNhapVao.startsWith("$2") || matKhauNhapVao.length() < 60) {
			// Đây là mật khẩu thô -> cần băm trước khi lưu
			matKhauLuuDB = PasswordUtils.hashPassword(matKhauNhapVao);
		}
		// Nếu đã là bcrypt thì giữ nguyên matKhauLuuDB = matKhauNhapVao

		return daoTaiKhoan.capNhatTaiKhoanTheoMaNV(maNV, tenDangNhap, matKhauLuuDB);
	}

	// =========================================================
	// 4. ĐỔI MẬT KHẨU CÁ NHÂN (người dùng tự đổi)
	// =========================================================
	/**
	 * Cho phép người dùng đổi mật khẩu của chính mình. Phải xác thực đúng mật khẩu
	 * hiện tại trước, sau đó mới cập nhật mật khẩu mới.
	 *
	 * @param tenDangNhap    Tên đăng nhập của người dùng
	 * @param matKhauHienTai Mật khẩu hiện tại (dùng để xác thực trước khi đổi)
	 * @param matKhauMoiTho  Mật khẩu mới (dạng thô, chưa băm)
	 * @return true nếu đổi thành công
	 */
	public boolean doiMatKhau(String tenDangNhap, String matKhauHienTai, String matKhauMoiTho) {
		// Bước 1: Xác thực mật khẩu hiện tại có đúng không
		if (authenticate(tenDangNhap, matKhauHienTai)) {
			// Bước 2: Kiểm tra mật khẩu mới có đủ mạnh không
			if (validateMatKhauMoi(matKhauMoiTho)) {
				// Bước 3: Băm mật khẩu mới và lưu xuống DB
				return daoTaiKhoan.capNhatMatKhau(tenDangNhap, PasswordUtils.hashPassword(matKhauMoiTho));
			}
		}
		return false; // Mật khẩu hiện tại sai hoặc mật khẩu mới không hợp lệ
	}

	// =========================================================
	// 5. QUÊN MẬT KHẨU - Khôi phục qua email
	// =========================================================
	/**
	 * Cập nhật mật khẩu mới thông qua email (chức năng quên mật khẩu). Không cần
	 * xác thực mật khẩu cũ, chỉ cần email hợp lệ.
	 *
	 * @param email         Email đã đăng ký của tài khoản
	 * @param matKhauMoiTho Mật khẩu mới muốn đặt (dạng thô)
	 * @return true nếu cập nhật thành công
	 */
	public boolean capNhatMatKhauTheoEmail(String email, String matKhauMoiTho) {
		// Kiểm tra mật khẩu mới có đủ mạnh không trước khi lưu
		if (validateMatKhauMoi(matKhauMoiTho)) {
			return daoTaiKhoan.capNhatMatKhauTheoEmail(email, PasswordUtils.hashPassword(matKhauMoiTho));
		}
		return false;
	}

	// =========================================================
	// 6. KIỂM TRA ĐỘ MẠNH MẬT KHẨU
	// =========================================================
	/**
	 * Kiểm tra mật khẩu mới có đủ điều kiện không. Điều kiện: tối thiểu 8 ký tự,
	 * phải có ít nhất 1 chữ cái và 1 chữ số.
	 *
	 * @param matKhauMoi Chuỗi mật khẩu cần kiểm tra
	 * @return true nếu hợp lệ
	 */
	public boolean validateMatKhauMoi(String matKhauMoi) {
		if (matKhauMoi == null || matKhauMoi.length() < 8)
			return false; // Quá ngắn

		boolean hasLetter = false; // Cờ kiểm tra có chữ cái
		boolean hasDigit = false; // Cờ kiểm tra có chữ số

		// Duyệt từng ký tự trong mật khẩu
		for (char c : matKhauMoi.toCharArray()) {
			if (Character.isLetter(c))
				hasLetter = true; // Tìm thấy chữ cái
			if (Character.isDigit(c))
				hasDigit = true; // Tìm thấy chữ số
		}

		return hasLetter && hasDigit; // Phải có cả hai mới hợp lệ
	}

	// =========================================================
	// 7. LẤY THÔNG TIN TÀI KHOẢN ĐẦY ĐỦ (kèm thông tin nhân viên)
	// =========================================================
	/**
	 * Lấy đối tượng TaiKhoan đầy đủ, bao gồm toàn bộ thông tin NhanVien liên kết.
	 * Dùng khi cần hiển thị hồ sơ người dùng sau đăng nhập.
	 *
	 * @param tenDangNhap Tên đăng nhập cần tra cứu
	 * @return TaiKhoan với NhanVienId đã được nạp đầy đủ dữ liệu, hoặc null nếu
	 *         không tìm thấy
	 */
	public TaiKhoan getTaiKhoanDayDu(String tenDangNhap) {
		TaiKhoan tk = daoTaiKhoan.getTaiKhoan(tenDangNhap); // Lấy tài khoản từ DB

		// Nếu không có tài khoản hoặc không liên kết nhân viên nào -> trả về nguyên xi
		if (tk == null || tk.getNhanVienId() == null)
			return tk;

		try {
			// Khởi tạo DAO nhân viên để tra cứu thêm
			DAO.DAO_NhanVien daoNV = new DAO.DAO_NhanVien();
			String maNV = tk.getNhanVienId().getNhanVien(); // Lấy mã nhân viên từ tài khoản

			List<Entity.NhanVien> dsNV = daoNV.layDSNhanVien(); // Lấy toàn bộ danh sách nhân viên

			// Tìm đúng nhân viên có mã khớp với mã trong tài khoản
			for (Entity.NhanVien nv : dsNV) {
				if (nv.getNhanVien().equals(maNV)) {
					tk.setNhanVienId(nv); // Gán đối tượng NhanVien đầy đủ vào tài khoản
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace(); // In lỗi ra console nếu có exception
		}

		return tk; // Trả về tài khoản đã được nạp đầy đủ thông tin nhân viên
	}

	public boolean datLaiMatKhauTheoEmail(String email, String matKhauMoi) {
		if (email == null || email.trim().isEmpty()) {
			return false;
		}

		if (matKhauMoi == null || matKhauMoi.trim().length() < 6) {
			return false;
		}

		String matKhauDaBam = PasswordUtils.hashPassword(matKhauMoi.trim());

		return daoTaiKhoan.capNhatMatKhauTheoEmail(email.trim(), matKhauDaBam);
	}
}