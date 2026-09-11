// 商品轮播图。支付、锁单和结算逻辑统一放在 index.html 的业务脚本中，
// 避免同一个按钮被旧脚本和新脚本重复绑定点击事件。
const swiperWrapper = document.querySelector(".swiper-wrapper");
const pagination = document.querySelector(".swiper-pagination");
const slides = document.querySelectorAll(".swiper-slide");

if (swiperWrapper && pagination && slides.length > 0) {
    let currentIndex = 0;

    slides.forEach((_, index) => {
        const dot = document.createElement("div");
        dot.className = `swiper-dot${index === 0 ? " active" : ""}`;
        pagination.appendChild(dot);
    });

    setInterval(() => {
        currentIndex = (currentIndex + 1) % slides.length;
        swiperWrapper.style.transform =
            `translateX(-${currentIndex * 100}%)`;

        document.querySelectorAll(".swiper-dot")
            .forEach((dot, index) => {
                dot.className =
                    `swiper-dot${index === currentIndex ? " active" : ""}`;
            });
    }, 3000);
}
