/**
 * 工具函数
 */

const AVATAR_STYLES = [
    { bg: '#1AAD19', fg: '#FFFFFF', pattern: 'circle' },   // 绿
    { bg: '#FA9D3B', fg: '#FFFFFF', pattern: 'diamond' },  // 橙
    { bg: '#576B95', fg: '#FFFFFF', pattern: 'square' },   // 蓝灰
    { bg: '#E64340', fg: '#FFFFFF', pattern: 'triangle' }, // 红
    { bg: '#10AEFF', fg: '#FFFFFF', pattern: 'hexagon' },  // 天蓝
    { bg: '#9C27B0', fg: '#FFFFFF', pattern: 'star' },     // 紫
    { bg: '#FF6B6B', fg: '#FFFFFF', pattern: 'cross' },    // 珊瑚
    { bg: '#2E7D32', fg: '#FFFFFF', pattern: 'dots' },     // 深绿
    { bg: '#C13E35', fg: '#FFFFFF', pattern: 'wave' },     // 酒红
    { bg: '#7B68EE', fg: '#FFFFFF', pattern: 'bolt' },     // 靛蓝
];

export function getAvatarColor(userId) {
    return AVATAR_STYLES[(userId - 1) % AVATAR_STYLES.length].bg;
}

export function getAvatarStyle(userId) {
    return AVATAR_STYLES[(userId - 1) % AVATAR_STYLES.length];
}

/**
 * 生成 SVG 头像 — 带图案背景 + 白色首字
 */
export function avatarSVG(userId, name, size = 40) {
    const style = getAvatarStyle(userId);
    const initial = name.charAt(0);
    const half = size / 2;
    const fontSize = Math.round(size * 0.42);
    let shapes = '';

    switch (style.pattern) {
        case 'circle':
            shapes = `<circle cx="${half}" cy="${half*0.7}" r="${size*0.18}" fill="rgba(255,255,255,0.15)"/>
                      <circle cx="${size*0.75}" cy="${size*0.8}" r="${size*0.12}" fill="rgba(255,255,255,0.1)"/>`;
            break;
        case 'diamond':
            shapes = `<rect x="${half*0.6}" y="${half*0.4}" width="${size*0.22}" height="${size*0.22}" rx="2" fill="rgba(255,255,255,0.15)" transform="rotate(45 ${half*0.7} ${half*0.55})"/>`;
            break;
        case 'square':
            shapes = `<rect x="${size*0.1}" y="${size*0.15}" width="${size*0.25}" height="${size*0.25}" rx="3" fill="rgba(255,255,255,0.12)"/>`;
            break;
        case 'triangle':
            shapes = `<polygon points="${half},${size*0.1} ${size*0.8},${size*0.6} ${size*0.2},${size*0.6}" fill="rgba(255,255,255,0.1)"/>`;
            break;
        case 'hexagon':
            shapes = `<circle cx="${half}" cy="${half}" r="${size*0.35}" fill="none" stroke="rgba(255,255,255,0.12)" stroke-width="2"/>`;
            break;
        case 'star':
            shapes = `<circle cx="${size*0.8}" cy="${size*0.2}" r="${size*0.08}" fill="rgba(255,255,255,0.2)"/>
                      <circle cx="${size*0.15}" cy="${size*0.85}" r="${size*0.06}" fill="rgba(255,255,255,0.15)"/>`;
            break;
        case 'cross':
            shapes = `<line x1="${size*0.15}" y1="${size*0.2}" x2="${size*0.35}" y2="${size*0.4}" stroke="rgba(255,255,255,0.15)" stroke-width="3"/>
                      <line x1="${size*0.35}" y1="${size*0.2}" x2="${size*0.15}" y2="${size*0.4}" stroke="rgba(255,255,255,0.15)" stroke-width="3"/>`;
            break;
        case 'dots':
            shapes = `<circle cx="${size*0.2}" cy="${size*0.25}" r="${size*0.04}" fill="rgba(255,255,255,0.2)"/>
                      <circle cx="${size*0.5}" cy="${size*0.15}" r="${size*0.03}" fill="rgba(255,255,255,0.15)"/>
                      <circle cx="${size*0.8}" cy="${size*0.3}" r="${size*0.05}" fill="rgba(255,255,255,0.12)"/>`;
            break;
        case 'wave':
            shapes = `<path d="M0,${size*0.3} Q${half},${size*0.1} ${size},${size*0.3}" fill="none" stroke="rgba(255,255,255,0.12)" stroke-width="2"/>`;
            break;
        case 'bolt':
            shapes = `<polygon points="${half},${size*0.1} ${size*0.65},${size*0.45} ${half*0.85},${size*0.45} ${half*1.1},${size*0.9} ${half*0.7},${size*0.5} ${half*0.9},${size*0.5}" fill="rgba(255,255,255,0.1)"/>`;
            break;
    }

    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 ${size} ${size}">
        <rect width="${size}" height="${size}" rx="6" fill="${style.bg}"/>
        ${shapes}
        <text x="${half}" y="${half + fontSize*0.35}" text-anchor="middle" fill="white"
              font-family="-apple-system,sans-serif" font-size="${fontSize}" font-weight="600">${initial}</text>
    </svg>`;
    return 'data:image/svg+xml,' + encodeURIComponent(svg);
}

export function getInitial(name) {
    return name.charAt(0);
}

export function formatTime(isoStr) {
    const d = new Date(isoStr);
    const now = new Date();
    const diff = (now - d) / 1000;
    if (diff < 60) return '刚刚';
    if (diff < 3600) return Math.floor(diff / 60) + '分钟前';
    if (diff < 86400) return Math.floor(diff / 3600) + '小时前';
    return `${d.getMonth() + 1}月${d.getDate()}日`;
}

export function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
