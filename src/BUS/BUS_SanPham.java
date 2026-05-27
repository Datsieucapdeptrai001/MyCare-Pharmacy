package BUS;

import DAO.DAO_SanPham;
import Entity.LoHang;
import Entity.SanPham;

import java.util.ArrayList;
import java.util.List;

public class BUS_SanPham {

    // Đối tượng DAO dùng để gọi xuống tầng Database
    // Khai báo final vì không cần thay đổi sau khi khởi tạo
    private final DAO_SanPham daoSanPham = new DAO_SanPham();

    /** Constructor mặc định — không cần khởi tạo gì thêm */
    public BUS_SanPham() {
    }

    // =========================================================================
    // NHÓM 1: TÌM KIẾM / TRA CỨU SẢN PHẨM
    // =========================================================================

    /**
     * Tra cứu sản phẩm theo từ khóa — dùng cho màn hình đổi trả hàng.
     * Không lọc theo tồn kho, trả về đầy đủ các sản phẩm khớp từ khóa.
     *
     * @param tuKhoa chuỗi tìm kiếm (tên, mã, hoạt chất, ...)
     * @return danh sách SanPham phù hợp
     */
    public List<SanPham> traCuuSanPham(String tuKhoa) {
        return daoSanPham.timKiemSanPhamDoiTra(tuKhoa);
    }

    /**
     * Tìm thuốc để đưa vào MẪU LIỀU (phác đồ điều trị).
     * Khác với tìm kiếm bán hàng: KHÔNG cần kiểm tra tồn kho,
     * chỉ cần thuốc tồn tại trong hệ thống là đủ.
     *
     * @param text từ khóa tìm kiếm
     * @return danh sách Object[] — mỗi phần tử là 1 dòng dữ liệu thuốc
     */
    public List<Object[]> timKiemThuocChoMauLieu(String text) {
        // Nếu ô tìm kiếm trống → trả về danh sách rỗng, không cần gọi DB
        if (isBlank(text)) return new ArrayList<>();
        return daoSanPham.timKiemThuocChoMauLieu(text.trim());
    }

    /**
     * Tìm kiếm sản phẩm để BÁN HÀNG.
     * Thường chỉ hiển thị sản phẩm còn tồn kho (logic lọc do DAO xử lý).
     *
     * @param text từ khóa (tên thuốc, mã, hoạt chất, mã vạch...)
     * @return danh sách Object[] — dữ liệu hiển thị trên bảng bán hàng
     */
    public List<Object[]> timKiemSanPhamBan(String text) {
        if (isBlank(text))
            return new ArrayList<>();
        return daoSanPham.timKiemSanPhamBan(text.trim());
    }

    // =========================================================================
    // NHÓM 2: LẤY THÔNG TIN ĐƠN VỊ, GIÁ, LÔ HÀNG
    // =========================================================================

    /**
     * Lấy % thuế VAT của sản phẩm theo tên.
     * Dùng khi cần tính giá bán có thuế mà chỉ biết tên (không có mã SP).
     *
     * @param tenSP tên sản phẩm
     * @return giá trị thuế VAT (0–100), trả về 0 nếu tên rỗng/null
     */
    public double layThueVATTheoTenSP(String tenSP) {
        if (tenSP == null || tenSP.trim().isEmpty())
            return 0;
        return daoSanPham.layThueVATTheoTenSP(tenSP.trim());
    }

    /**
     * Tính giá bán theo đơn vị muốn bán (ví dụ: hộp, vỉ, viên...).
     *
     * Cách hoạt động:
     *   1. Lấy danh sách đơn vị quy đổi của sản phẩm từ DB
     *   2. So khớp đơn vị muốn bán với từng dòng trong danh sách
     *   3. Nếu tìm thấy → trả về giá quy đổi tương ứng
     *   4. Nếu không tìm thấy → fallback về giá bán gốc của sản phẩm
     *
     * @param maSP          mã sản phẩm
     * @param donViMuonBan  tên đơn vị muốn bán (VD: "Hộp", "Vỉ", "Viên")
     * @return giá bán tương ứng với đơn vị, hoặc 0 nếu không tìm được
     */
    public double tinhGiaBanTheoDonVi(String maSP, String donViMuonBan) {
        if (isBlank(maSP) || isBlank(donViMuonBan))
            return 0.0;

        // Bước 1: Lấy danh sách quy đổi đơn vị của SP từ DB
        List<Object[]> list = daoSanPham.layDonViQuyDoiTheoSP(maSP.trim());

        // Bước 2: Duyệt danh sách, so khớp tên đơn vị (không phân biệt hoa/thường)
        for (Object[] row : list) {
            if (donViMuonBan.trim().equalsIgnoreCase(row[0].toString())) {
                try {
                    // row[2] là giá quy đổi — cần xóa dấu phẩy nếu có (VD: "1,500" → 1500)
                    return Double.parseDouble(row[2].toString().replace(",", "").trim());
                } catch (Exception ignored) {
                    // Nếu parse lỗi thì bỏ qua, tiếp tục fallback
                }
            }
        }

        // Bước 3 (fallback): Lấy giá gốc nếu không tìm được đơn vị quy đổi
        SanPham sp = daoSanPham.getSanPhamDayDu(maSP.trim());
        return sp != null ? sp.getGiaBan() : 0.0;
    }

    /**
     * Lấy TẤT CẢ lô hàng của sản phẩm (kể cả lô đã hết hoặc hết hạn).
     * Dùng cho màn hình quản lý lô, xem lịch sử nhập hàng.
     */
    public List<LoHang> layTatCaLoTheoSP(String maSP) {
        if (isBlank(maSP))
            return new ArrayList<>();
        return daoSanPham.layTatCaLoTheoSP(maSP.trim());
    }

    /**
     * Lấy lô hàng CÒN HÀNG của sản phẩm (dùng khi bán hàng).
     * Thường lọc các lô còn số lượng > 0 và chưa hết hạn.
     */
    public List<LoHang> layLoTheoSP(String maSP) {
        if (isBlank(maSP))
            return new ArrayList<>();
        return daoSanPham.layLoTheoSP(maSP.trim());
    }

    /**
     * Lấy danh sách đơn vị đo lường của sản phẩm (viên, vỉ, hộp...).
     * Dùng để populate combobox chọn đơn vị trên màn hình bán hàng.
     */
    public List<Object[]> layDonViTheoSP(String maSP) {
        if (isBlank(maSP))
            return new ArrayList<>();
        return daoSanPham.layDonViDoLuongTheoSP(maSP.trim());
    }

    // =========================================================================
    // NHÓM 3: VALIDATE DỮ LIỆU SẢN PHẨM
    // =========================================================================

    /**
     * Kiểm tra tính hợp lệ của một đối tượng SanPham trước khi lưu.
     * Các quy tắc nghiệp vụ:
     *   - SP không được null
     *   - Mã SP và Tên SP không được rỗng
     *   - Tên SP không quá 150 ký tự
     *   - Thuế VAT phải trong khoảng [0, 100]
     *   - Giá bán không được âm
     *
     * @param sp đối tượng SanPham cần kiểm tra
     * @return true nếu hợp lệ, false nếu vi phạm bất kỳ quy tắc nào
     */
    public boolean kiemTraThongTinSP(SanPham sp) {
        if (sp == null)
            return false;
        if (isBlank(sp.getId()))
            return false;
        if (isBlank(sp.getTen()))
            return false;
        if (sp.getTen().trim().length() > 150)   // Giới hạn độ dài tên
            return false;
        if (sp.getThueVAT() < 0 || sp.getThueVAT() > 100) // VAT hợp lệ: 0%-100%
            return false;
        if (sp.getGiaBan() < 0)                  // Giá bán không âm
            return false;
        return true;
    }

    // =========================================================================
    // NHÓM 4: THÊM SẢN PHẨM (các overload themSP)
    // =========================================================================

    /**
     * Thêm sản phẩm MỚI — phiên bản đơn giản (không có đơn vị quy đổi).
     * Dùng khi chỉ cần lưu thông tin cơ bản của sản phẩm.
     *
     * Validate trước: id và ten không rỗng, vat >= 0, giaBan >= 0.
     * Các trường còn lại được bọc qua safe() để tránh null.
     */
    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy) {
        // Validate bắt buộc: mã, tên, vat hợp lệ, giá không âm
        if (isBlank(id) || isBlank(ten) || vat < 0 || giaBan < 0)
            return false;

        return daoSanPham.themSanPhamNhanh(
                id.trim(),
                safe(danhMuc).trim(),   // safe() đảm bảo không pass null xuống DB
                safe(dang).trim(),
                ten.trim(),
                safe(vietTat).trim(),
                safe(nsx).trim(),
                safe(hoatChat).trim(),
                vat,
                safe(hamLuong).trim(),
                safe(moTa).trim(),
                safe(dvt).trim(),
                giaBan,
                safe(maVach).trim(),
                safe(nhomBenhLy).trim());
    }

    /**
     * Thêm sản phẩm — có đơn vị quy đổi, KHÔNG có vị trí (viTriId = "").
     * Delegate sang overload chính bên dưới với viTriId rỗng.
     */
    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy, List<Object[]> dsDonVi) {
        // Gọi sang overload đầy đủ, truyền viTriId = ""
        return themSP(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat,
                hamLuong, moTa, dvt, giaBan, maVach, nhomBenhLy, "", dsDonVi);
    }

    /**
     * Thêm sản phẩm — OVERLOAD ĐẦY ĐỦ NHẤT: có vị trí + đơn vị quy đổi.
     *
     * Quy trình:
     *   1. Validate đầu vào
     *   2. Lưu thông tin sản phẩm vào DB (themSanPhamNhanh)
     *   3. Nếu thêm thành công VÀ có danh sách đơn vị → lưu đơn vị quy đổi
     *
     * @param viTriId   mã vị trí kệ thuốc (có thể rỗng)
     * @param dsDonVi   danh sách đơn vị quy đổi (VD: 1 hộp = 10 vỉ = 100 viên)
     */
    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy, String viTriId, List<Object[]> dsDonVi) {
        if (isBlank(id) || isBlank(ten) || vat < 0 || giaBan < 0) return false;

        // Bước 1: Lưu sản phẩm
        boolean ok = daoSanPham.themSanPhamNhanh(
                id.trim(), safe(danhMuc).trim(), safe(dang).trim(), ten.trim(),
                safe(vietTat).trim(), safe(nsx).trim(), safe(hoatChat).trim(), vat,
                safe(hamLuong).trim(), safe(moTa).trim(), safe(dvt).trim(), giaBan,
                safe(maVach).trim(), safe(nhomBenhLy).trim(), safe(viTriId).trim());

        // Bước 2: Chỉ lưu đơn vị quy đổi nếu thêm SP thành công và có dữ liệu đơn vị
        if (ok && dsDonVi != null) daoSanPham.luuDonViQuyDoi(id.trim(), dvt, giaBan, dsDonVi);
        return ok;
    }

    /**
     * Thêm sản phẩm — có đơn vị quy đổi, KHÔNG có mã vạch và nhóm bệnh lý.
     * Dùng khi form nhập không yêu cầu mã vạch / nhóm bệnh lý.
     */
    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan, List<Object[]> dsDonVi) {
        // maVach = "", nhomBenhLy = "" → truyền chuỗi rỗng thay vì null
        return themSP(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat,
                hamLuong, moTa, dvt, giaBan, "", "", dsDonVi);
    }

    // =========================================================================
    // NHÓM 5: CẬP NHẬT SẢN PHẨM (các overload capNhatSP)
    // =========================================================================
    // Cấu trúc tương tự nhóm themSP — cũng có 4 overload:
    //   (1) Không có đơn vị quy đổi, không vị trí
    //   (2) Có đơn vị, không vị trí
    //   (3) OVERLOAD ĐẦY ĐỦ: có đơn vị + vị trí
    //   (4) Không có mã vạch và nhóm bệnh lý

    /** Cập nhật SP — phiên bản đơn giản (không đơn vị quy đổi, không vị trí) */
    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy) {
        if (isBlank(id) || isBlank(ten) || vat < 0 || giaBan < 0)
            return false;
        return daoSanPham.capNhatSanPhamNhanh(
                id.trim(), safe(danhMuc).trim(), safe(dang).trim(), ten.trim(),
                safe(vietTat).trim(), safe(nsx).trim(), safe(hoatChat).trim(), vat,
                safe(hamLuong).trim(), safe(moTa).trim(), safe(dvt).trim(), giaBan,
                safe(maVach).trim(), safe(nhomBenhLy).trim());
    }

    /** Cập nhật SP — có đơn vị quy đổi, không vị trí → delegate sang overload chính */
    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy, List<Object[]> dsDonVi) {
        return capNhatSP(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat,
                hamLuong, moTa, dvt, giaBan, maVach, nhomBenhLy, "", dsDonVi);
    }

    /**
     * Cập nhật SP — OVERLOAD ĐẦY ĐỦ NHẤT: có vị trí + đơn vị quy đổi.
     *
     * Khác với themSP: luuDonViQuyDoi được gọi kể cả khi dsDonVi = null
     * (để xóa đơn vị cũ nếu người dùng xóa hết đơn vị quy đổi).
     */
    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy, String viTriId, List<Object[]> dsDonVi) {
        if (isBlank(id) || isBlank(ten) || vat < 0 || giaBan < 0) return false;

        boolean ok = daoSanPham.capNhatSanPhamNhanh(
                id.trim(), safe(danhMuc).trim(), safe(dang).trim(), ten.trim(),
                safe(vietTat).trim(), safe(nsx).trim(), safe(hoatChat).trim(), vat,
                safe(hamLuong).trim(), safe(moTa).trim(), safe(dvt).trim(), giaBan,
                safe(maVach).trim(), safe(nhomBenhLy).trim(), safe(viTriId).trim());

        // Luôn lưu lại đơn vị quy đổi sau khi cập nhật (kể cả khi dsDonVi null → xóa cũ)
        if (ok) daoSanPham.luuDonViQuyDoi(id.trim(), dvt, giaBan, dsDonVi);
        return ok;
    }

    /** Cập nhật SP — không có mã vạch và nhóm bệnh lý → truyền chuỗi rỗng */
    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan, List<Object[]> dsDonVi) {
        return capNhatSP(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat,
                hamLuong, moTa, dvt, giaBan, "", "", dsDonVi);
    }

    /**
     * Cập nhật MÃ VẠCH cho sản phẩm (cập nhật riêng, không cần sửa toàn bộ SP).
     *
     * Có in DEBUG ra console để tiện kiểm tra khi test.
     * Validate: cả maSP lẫn maVachMoi đều không được rỗng.
     *
     * @param maSP       mã sản phẩm cần cập nhật
     * @param maVachMoi  mã vạch mới
     * @return true nếu cập nhật thành công
     */
    public boolean capNhatMaVachSanPham(String maSP, String maVachMoi) {
        // In log debug để dễ trace khi có lỗi
        System.out.println("=== DEBUG BUS_SanPham.capNhatMaVachSanPham ===");
        System.out.println("maSP = [" + maSP + "]");
        System.out.println("maVachMoi = [" + maVachMoi + "]");

        if (isBlank(maSP) || isBlank(maVachMoi)) {
            System.out.println("BUS result = false vì mã sản phẩm hoặc mã vạch rỗng");
            return false;
        }

        boolean ok = daoSanPham.capNhatMaVachSanPham(maSP.trim(), maVachMoi.trim());
        System.out.println("BUS result = " + ok);
        return ok;
    }

    // =========================================================================
    // NHÓM 6: XÓA / ẨN / KHÔI PHỤC SẢN PHẨM
    // =========================================================================

    /**
     * "Xóa" sản phẩm — thực chất là ẨN (soft delete), không xóa vật lý khỏi DB.
     * Delegate sang anSP() — thiết kế này giúp GUI gọi xoaSP() mà không cần biết
     * bên trong là ẩn hay xóa thật.
     *
     * @param id mã sản phẩm cần xóa
     */
    public boolean xoaSP(String id) {
        return anSP(id);  // "Xóa" = Ẩn SP (không xóa khỏi DB)
    }

    /**
     * Ẩn sản phẩm khỏi danh sách hiển thị (soft delete).
     *
     * Quy tắc nghiệp vụ QUAN TRỌNG:
     *   → Chỉ được ẩn nếu tồn kho = 0.
     *   → Nếu còn hàng trong kho → từ chối, trả về false.
     * Lý do: tránh trường hợp sản phẩm bị ẩn nhưng vẫn còn trong kho.
     *
     * @param maSP mã sản phẩm cần ẩn
     */
    public boolean anSP(String maSP) {
        if (isBlank(maSP))
            return false;

        // Kiểm tra tồn kho trước khi ẩn
        int ton = daoSanPham.getSoLuongTon(maSP.trim());

        // Nghiệp vụ: còn hàng → không cho ẩn
        if (ton > 0)
            return false;

        return daoSanPham.anSanPham(maSP.trim());
    }

    /**
     * Lấy số lượng tồn kho hiện tại của sản phẩm.
     * Dùng để kiểm tra trước khi cho phép xóa/ẩn hoặc hiển thị cảnh báo.
     */
    public int getSoLuongTon(String maSP) {
        if (isBlank(maSP))
            return 0;
        return daoSanPham.getSoLuongTon(maSP.trim());
    }

    /**
     * Lấy danh sách các sản phẩm đã bị ẨN.
     * Dùng cho màn hình "thùng rác" / quản lý SP đã ngừng kinh doanh.
     */
    public List<Object[]> layDanhSachSanPhamDaAn() {
        return daoSanPham.layDanhSachSanPhamDaAn();
    }

    /**
     * Khôi phục sản phẩm đã bị ẩn về trạng thái hiển thị bình thường.
     * Dùng khi muốn kinh doanh lại sản phẩm đã ngừng.
     */
    public boolean khoiPhucSP(String maSP) {
        if (isBlank(maSP))
            return false;
        return daoSanPham.khoiPhucSanPham(maSP.trim());
    }

    // =========================================================================
    // NHÓM 7: LẤY DANH SÁCH / THÔNG TIN SẢN PHẨM
    // =========================================================================

    /** Lấy danh sách SP dạng Object[] để hiển thị lên JTable / bảng dữ liệu */
    public List<Object[]> layDanhSachChoBang() {
        return daoSanPham.layDanhSachSanPhamChoBang();
    }

    /** Lấy danh sách tất cả thuốc (dùng cho autocomplete, combo box,...) */
    public List<SanPham> getDsThuoc() {
        return daoSanPham.getDsThuoc();
    }

    /**
     * Lấy đầy đủ thông tin sản phẩm (tất cả các trường) theo mã SP.
     * Dùng khi cần điền sẵn form chỉnh sửa hoặc xem chi tiết.
     *
     * @return đối tượng SanPham đầy đủ, hoặc null nếu không tìm thấy / mã rỗng
     */
    public SanPham getSanPhamDayDu(String maSP) {
        if (isBlank(maSP))
            return null;
        return daoSanPham.getSanPhamDayDu(maSP.trim());
    }

    /** Lấy danh sách đơn vị quy đổi của SP (hộp → vỉ → viên, kèm tỉ lệ và giá) */
    public List<Object[]> layDonViQuyDoiTheoSP(String maSP) {
        if (isBlank(maSP))
            return new ArrayList<>();
        return daoSanPham.layDonViQuyDoiTheoSP(maSP.trim());
    }

    // =========================================================================
    // NHÓM 8: LẤY DỮ LIỆU DANH MỤC (dùng populate ComboBox)
    // =========================================================================

    /** Danh sách tên nhà sản xuất → populate ComboBox "Nhà sản xuất" */
    public List<String> layDanhSachNhaSanXuat() {
        return daoSanPham.layDanhSachNhaSanXuat();
    }

    /** Danh sách đơn vị tính (viên, vỉ, hộp, chai...) → ComboBox "ĐVT" */
    public List<String> layDanhSachDonViTinh() {
        return daoSanPham.layDanhSachDonViTinh();
    }

    /** Danh sách tên sản phẩm → dùng cho autocomplete / gợi ý tìm kiếm */
    public List<String> layDanhSachTenSanPham() {
        return daoSanPham.layDanhSachTenSanPham();
    }

    /** Danh sách nhóm bệnh lý → ComboBox "Nhóm bệnh lý" */
    public List<String> layDanhSachNhomBenhLy() {
        return daoSanPham.layDanhSachNhomBenhLy();
    }

    // =========================================================================
    // NHÓM 9: KIỂM TRA TRÙNG LẶP
    // =========================================================================

    /**
     * Kiểm tra mã SP đã tồn tại trong DB chưa.
     * Dùng trước khi thêm SP mới để tránh trùng mã.
     */
    public boolean kiemTraMaSPTonTai(String id) {
        if (isBlank(id))
            return false;
        return daoSanPham.kiemTraMaSPTonTai(id.trim());
    }

    /**
     * Kiểm tra mã vạch đã được dùng bởi SP khác chưa.
     *
     * @param maVach     mã vạch cần kiểm tra
     * @param maSPBoQua  mã SP được phép bỏ qua (dùng khi SỬA — bỏ qua chính SP đang sửa)
     * @return true nếu mã vạch đã tồn tại ở SP khác
     */
    public boolean kiemTraMaVachTonTai(String maVach, String maSPBoQua) {
        if (isBlank(maVach)) {
            return false;
        }
        return daoSanPham.kiemTraMaVachTonTai(maVach.trim(), maSPBoQua);
    }

    // =========================================================================
    // NHÓM 10: TẠO MÃ TỰ ĐỘNG
    // =========================================================================

    /**
     * Tạo mã sản phẩm mới tự động (thường theo định dạng SP001, SP002...).
     * Gọi khi mở form thêm SP mới để tự điền sẵn mã vào ô input.
     */
    public String taoMaMoi() {
        return daoSanPham.layMaSanPhamMoiNhat();
    }

    // =========================================================================
    // NHÓM 11: TIỆN ÍCH NỘI BỘ (private helpers)
    // =========================================================================

    /**
     * Kiểm tra chuỗi rỗng hoặc null.
     * Dùng nội bộ để validate đầu vào trước khi gọi DAO.
     * Tách ra helper riêng để tránh lặp code null-check ở khắp nơi.
     *
     * @return true nếu chuỗi là null hoặc chỉ chứa khoảng trắng
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Trả về chuỗi rỗng "" nếu s là null, ngược lại trả về s.
     * Dùng cho các trường KHÔNG BẮT BUỘC (danhMuc, nsx, hoatChat...)
     * để tránh NullPointerException khi gọi .trim() hoặc truyền xuống DAO.
     */
    private String safe(String s) {
        return s == null ? "" : s;
    }

    // =========================================================================
    // NHÓM 12: VỊ TRÍ THUỐC (kệ/tủ lưu trữ thuốc)
    // Gộp vào BUS_SanPham thay vì tách class riêng, dùng inner class ViTriThuoc
    // =========================================================================

    /** Lấy tất cả vị trí kệ thuốc trong kho */
    public List<SanPham.ViTriThuoc> layTatCaViTri() {
        return daoSanPham.layTatCaViTri();
    }

    /**
     * Lấy thông tin chi tiết 1 vị trí theo ID.
     * @return null nếu id rỗng hoặc không tìm thấy
     */
    public SanPham.ViTriThuoc layViTriTheoId(String id) {
        if (isBlank(id)) return null;
        return daoSanPham.layViTriTheoId(id.trim());
    }

    /** Lấy danh sách tên khu (khu A, B, C...) → populate ComboBox chọn khu */
    public List<String> layDanhSachKhuViTri() {
        return daoSanPham.layDanhSachKhuViTri();
    }

    /**
     * Thêm vị trí kệ thuốc mới.
     * Bắt buộc: id, khu, kệ không được rỗng.
     */
    public boolean themViTri(SanPham.ViTriThuoc vt) {
        if (vt == null || isBlank(vt.getId()) || isBlank(vt.getKhu()) || isBlank(vt.getKe()))
            return false;
        return daoSanPham.themViTri(vt);
    }

    /**
     * Cập nhật thông tin vị trí kệ thuốc.
     * Chỉ cần id hợp lệ là đủ để cập nhật.
     */
    public boolean capNhatViTri(SanPham.ViTriThuoc vt) {
        if (vt == null || isBlank(vt.getId())) return false;
        return daoSanPham.capNhatViTri(vt);
    }

    /**
     * Xóa vị trí kệ thuốc — có kiểm tra nghiệp vụ.
     *
     * Quy tắc: không được xóa vị trí nếu còn sản phẩm đang gán vào đó.
     *
     * @return  "OK"     — xóa thành công
     *          "CON_SP" — vị trí vẫn còn SP đang dùng → từ chối xóa
     *          "LOI"    — lỗi khác (id rỗng, lỗi DB...)
     */
    public String xoaViTri(String id) {
        if (isBlank(id)) return "LOI";
        int kq = daoSanPham.xoaViTri(id.trim());
        if (kq == 0)  return "OK";      // DAO trả 0 = thành công
        if (kq == -1) return "CON_SP";  // DAO trả -1 = còn SP đang dùng vị trí này
        return "LOI";                   // Các mã lỗi khác
    }

    /**
     * Gán vị trí kệ cho sản phẩm.
     * viTriId có thể rỗng (null/blank) → hủy gán vị trí.
     *
     * @param maSP    mã sản phẩm (bắt buộc)
     * @param viTriId mã vị trí kệ (có thể null để bỏ gán)
     */
    public boolean ganViTriChoSP(String maSP, String viTriId) {
        if (isBlank(maSP)) return false;
        return daoSanPham.ganViTriChoSP(maSP.trim(), viTriId); // viTriId giữ nguyên (không trim) vì có thể null
    }

    /** Sinh ID vị trí mới tự động (tương tự taoMaMoi() nhưng cho vị trí) */
    public String layIdViTriMoi() {
        return daoSanPham.layIdViTriMoi();
    }

    // =========================================================================
    // NHÓM 13: NGHIỆP VỤ CẮT LIỀU / MẪU LIỀU (Phác đồ điều trị)
    // Tái sử dụng từ BUS_MauLieu, gộp vào đây để tiện truy cập
    // =========================================================================

    /**
     * Tìm sản phẩm theo mã vạch (quét barcode).
     * Dùng khi bán hàng qua máy quét mã vạch.
     *
     * @return SanPham nếu tìm thấy, null nếu mã vạch rỗng hoặc không tồn tại
     */
    public SanPham getSanPhamByBarcode(String maVach) {
        if (maVach == null || maVach.trim().isEmpty()) return null;
        return daoSanPham.getSanPhamByBarcode(maVach.trim());
    }

    /**
     * Lấy đơn vị NHỎ NHẤT của sản phẩm (ví dụ: "Viên" với tỉ lệ quy đổi = 1).
     * Dùng trong nghiệp vụ cắt liều để biết đơn vị tối thiểu có thể bán.
     *
     * @return Object[] chứa [tenDonVi, tiLeQuyDoi, giaBan] hoặc null
     */
    public Object[] getDonViNhoNhat(String maSP) {
        if (maSP == null || maSP.trim().isEmpty()) return null;
        return daoSanPham.getDonViNhoNhat(maSP.trim());
    }

    /**
     * Lấy danh sách tất cả MẪU LIỀU (combo phác đồ) nâng cao.
     * Dùng để hiển thị bảng chọn mẫu liều trên màn hình kê đơn.
     */
    public List<Object[]> layDanhSachComboNangCao() {
        return daoSanPham.layDanhSachComboNangCao();
    }

    /** Lấy danh sách nhóm bệnh lý dùng trong mẫu liều → populate ComboBox */
    public List<String> layDanhSachNhomBenhLieu() {
        return daoSanPham.layDanhSachNhomBenhLieu();
    }

    /**
     * Lấy chi tiết 1 mẫu liều (combo) theo ID.
     * Trả về MauLieu kèm danh sách ChiTietLieu bên trong.
     */
    public SanPham.MauLieu layComboByIdNangCao(String comboId) {
        if (comboId == null || comboId.trim().isEmpty()) return null;
        return daoSanPham.layComboByIdNangCao(comboId.trim());
    }

    /**
     * Tạo mẫu liều MỚI nâng cao.
     *
     * Quy trình:
     *   1. Validate: tên combo và danh sách chi tiết không được rỗng
     *   2. Sinh ID mới từ DB
     *   3. Gán comboId vào từng ChiTietLieu
     *   4. Lưu toàn bộ xuống DB
     *
     * @param dsChiTiet danh sách các thuốc trong phác đồ + liều lượng
     * @return ID của mẫu liều vừa tạo, hoặc null nếu thất bại
     */
    public String themMauMoiNangCao(String tenCombo, String nhomBenh, double giaBanCombo,
                                     String ghiChu, List<SanPham.ChiTietLieu> dsChiTiet) {
        if (tenCombo == null || tenCombo.trim().isEmpty() || dsChiTiet == null || dsChiTiet.isEmpty())
            return null;

        // Sinh ID mới cho mẫu liều
        String id = daoSanPham.sinhComboIdMoi();
        SanPham.MauLieu m = new SanPham.MauLieu(id, tenCombo.trim(), nhomBenh, giaBanCombo, ghiChu);

        // Gán comboId cho từng chi tiết liều trước khi lưu
        for (SanPham.ChiTietLieu ct : dsChiTiet) ct.setComboId(id);

        // Lưu xuống DB, trả về id nếu thành công, null nếu thất bại
        return daoSanPham.taoMauMoiNangCao(m, dsChiTiet) ? id : null;
    }

    /**
     * Cập nhật mẫu liều nâng cao.
     * Validate: comboId, tenCombo, và danh sách chi tiết đều không được null/rỗng.
     */
    public boolean capNhatMauNangCao(String comboId, String tenCombo, String nhomBenh,
                                      double giaBanCombo, String ghiChu,
                                      List<SanPham.ChiTietLieu> dsChiTiet) {
        if (comboId == null || tenCombo == null || dsChiTiet == null || dsChiTiet.isEmpty())
            return false;

        SanPham.MauLieu m = new SanPham.MauLieu(comboId.trim(), tenCombo.trim(), nhomBenh, giaBanCombo, ghiChu);

        // Đồng bộ comboId vào từng chi tiết liều
        for (SanPham.ChiTietLieu ct : dsChiTiet) ct.setComboId(comboId.trim());

        return daoSanPham.capNhatMauNangCao(m, dsChiTiet);
    }

    /**
     * Xóa mẫu liều nâng cao (xóa cả header lẫn chi tiết liều trong DB).
     */
    public boolean xoaMauNangCao(String comboId) {
        if (comboId == null || comboId.trim().isEmpty()) return false;
        return daoSanPham.xoaMauNangCao(comboId.trim());
    }

    /**
     * Kiểm tra tên combo (mẫu liều) đã tồn tại chưa.
     * Dùng để tránh tạo 2 phác đồ trùng tên.
     */
    public boolean kiemTraTenComboTonTai(String tenCombo) {
        if (tenCombo == null || tenCombo.trim().isEmpty()) return false;
        return daoSanPham.kiemTraTenComboTonTai(tenCombo.trim());
    }
}