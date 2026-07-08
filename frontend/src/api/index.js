/**
 * API 层 — 前后端契约
 *
 * USE_MOCK = true  → 使用模拟数据（讲师演示 / 学生后端未就绪时）
 * USE_MOCK = false → 调用真实后端 API（localhost:8080）
 *
 * 学生后端接口实现后，只需把 USE_MOCK 改为 false。
 */

const API_BASE = 'http://localhost:8080';
let USE_MOCK = false;

// ===================== Mock 数据 =====================

const MOCK_USERS = [
    // 普通用户（同学/朋友/家人风格）
    { id: 1,  name: '我',       avatar: '', isBigV: false, followerCount: 86,   relation: '自己' },
    { id: 2,  name: '老妈',     avatar: '', isBigV: false, followerCount: 23,   relation: '家人' },
    { id: 3,  name: '老王',     avatar: '', isBigV: false, followerCount: 152,  relation: '室友' },
    { id: 4,  name: '陈大佬',   avatar: '', isBigV: false, followerCount: 2300, relation: '学长' },
    { id: 5,  name: '小美',     avatar: '', isBigV: false, followerCount: 67,   relation: '同学' },
    // 大V（真实人名，活跃气氛）
    { id: 6,  name: '雷军',     avatar: '', isBigV: true,  followerCount: 23500000, relation: '大V' },
    { id: 7,  name: '余承东',   avatar: '', isBigV: true,  followerCount: 18600000, relation: '大V' },
    { id: 8,  name: '刘强东',   avatar: '', isBigV: true,  followerCount: 15200000, relation: '大V' },
    { id: 9,  name: '罗永浩',   avatar: '', isBigV: true,  followerCount: 9800000,  relation: '大V' },
    { id: 10, name: '导师',     avatar: '', isBigV: false, followerCount: 340,  relation: '老师' },
];

const MOCK_POSTS = [
    { id: 1, userId: 3,  content: '凌晨三点终于把推模式的批量写入调通了，INSERT一次插500条，从3秒降到30毫秒，爽！', createdAt: '2026-07-01T09:30:00', likeCount: 18, commentCount: 6, source: 'push', isPinned: false },
    { id: 2, userId: 6,  content: '小米汽车 SU7 Ultra 纽北成绩 6分46秒，中国品牌第一次登顶！', createdAt: '2026-07-01T09:15:00', likeCount: 89000, commentCount: 12000, source: 'pull', isPinned: false },
    { id: 3, userId: 2,  content: '儿子今天实训第一天，说要做微信朋友圈，晚上回家给我演示🥰', createdAt: '2026-07-01T08:50:00', likeCount: 5, commentCount: 3, source: 'push', isPinned: false },
    { id: 4, userId: 7,  content: '华为鸿蒙生态设备突破12亿台，感谢每一位开发者的支持！', createdAt: '2026-07-01T08:30:00', likeCount: 62000, commentCount: 8500, source: 'pull', isPinned: false },
    { id: 5, userId: 5,  content: '有人知道 MySQL 主从延迟怎么解决吗？配了一上午还是同步不上😭', createdAt: '2026-07-01T08:15:00', likeCount: 7, commentCount: 4, source: 'push', isPinned: false },
    { id: 6, userId: 8,  content: '京东物流无人机在西藏完成首次珠峰大本营配送，海拔5200米！', createdAt: '2026-07-01T07:45:00', likeCount: 45000, commentCount: 6700, source: 'pull', isPinned: false },
    { id: 7, userId: 4,  content: 'Feed系统架构经验：inbox表一定要建(user_id, created_at)联合索引，否则深翻页直接爆炸', createdAt: '2026-07-01T07:20:00', likeCount: 34, commentCount: 8, source: 'push', isPinned: false },
    { id: 8, userId: 9,  content: '交个朋友，今天直播间给大家演示一个 Feed 流系统是怎么从零搭建的', createdAt: '2026-07-01T06:50:00', likeCount: 31000, commentCount: 5200, source: 'pull', isPinned: false },
    { id: 9, userId: 10, content: '实训Day1，希望大家认真学习推拉结合的架构思想，这在工业界是非常经典的设计模式。', createdAt: '2026-07-01T06:30:00', likeCount: 12, commentCount: 2, source: 'push', isPinned: false },
];

const MOCK_COMMENTS = [
    { id: 1, postId: 1, userId: 5,  content: '卷王实锤', createdAt: '2026-07-01T09:35:00' },
    { id: 2, postId: 1, userId: 4,  content: '批量INSERT别忘了加 rewriteBatchedStatements 参数', createdAt: '2026-07-01T09:38:00' },
    { id: 3, postId: 1, userId: 10, content: '不错，注意事务边界', createdAt: '2026-07-01T09:42:00' },
    { id: 4, postId: 3, userId: 1,  content: '妈等我回去演示！', createdAt: '2026-07-01T08:55:00' },
    { id: 5, postId: 5, userId: 3,  content: 'binlog格式改成ROW试试', createdAt: '2026-07-01T08:20:00' },
    { id: 6, postId: 5, userId: 4,  content: '检查一下 server_id 有没有重复', createdAt: '2026-07-01T08:22:00' },
    { id: 7, postId: 7, userId: 1,  content: '学长yyds', createdAt: '2026-07-01T07:25:00' },
    { id: 8, postId: 7, userId: 5,  content: '覆盖索引！覆盖索引！覆盖索引！重要的事说三遍', createdAt: '2026-07-01T07:28:00' },
];

let currentUserId = 1;

// 模拟屏蔽的用户 ID 集合（mock 模式模拟屏蔽效果）
const MOCK_BLOCKED_IDS = new Set();

// ===================== 通用请求 =====================

async function request(method, path, body) {
    if (USE_MOCK) return null;
    const opts = { method, headers: { 'Content-Type': 'application/json' } };
    if (body) opts.body = JSON.stringify(body);
    const res = await fetch(API_BASE + path, opts);
    if (!res.ok) throw new Error(`API ${path} returned ${res.status}`);
    return res.json();
}

// ===================== 用户接口 =====================

export async function getUserList() {
    if (USE_MOCK) return MOCK_USERS;
    return request('GET', '/api/user/list');
}

export async function switchUser(userId) {
    currentUserId = userId;
    if (USE_MOCK) {
        MOCK_BLOCKED_IDS.clear();
        return MOCK_USERS.find(u => u.id === userId);
    }
    return request('POST', '/api/user/switch', { userId });
}

export async function getUserProfile() {
    if (USE_MOCK) {
        const u = MOCK_USERS.find(u => u.id === currentUserId);
        return { ...u, followingCount: 50, postCount: 12 };
    }
    return request('GET', '/api/user/profile');
}

export function getCurrentUserId() {
    return currentUserId;
}

// ===================== Feed 接口 =====================

export async function getTimeline(cursor, size = 20) {
    if (USE_MOCK) {
        let posts = [...MOCK_POSTS];
        if (cursor) posts = posts.filter(p => p.id < cursor);
        // mock 屏蔽过滤
        posts = posts.filter(p => !MOCK_BLOCKED_IDS.has(p.userId));
        return { list: posts.slice(0, size), hasMore: posts.length > size, nextCursor: posts[size]?.id };
    }
    const params = new URLSearchParams({ size });
    if (cursor) params.set('cursor', cursor);
    return request('GET', `/api/feed/timeline?${params}`);
}

export async function getInbox(cursor, size = 20) {
    if (USE_MOCK) {
        return { list: MOCK_POSTS.filter(p => p.source === 'push'), hasMore: false, nextCursor: null };
    }
    const params = new URLSearchParams({ size });
    if (cursor) params.set('cursor', cursor);
    return request('GET', `/api/feed/inbox?${params}?${params}`);
}

export async function getOutbox(cursor, size = 20) {
    if (USE_MOCK) {
        return { list: MOCK_POSTS.filter(p => p.source === 'pull'), hasMore: false, nextCursor: null };
    }
    const params = new URLSearchParams({ size });
    if (cursor) params.set('cursor', cursor);
    return request('GET', `/api/feed/outbox?${params}`);
}

export async function createPost(content) {
    if (USE_MOCK) {
        const newPost = {
            id: Date.now(),
            userId: currentUserId,
            content,
            createdAt: new Date().toISOString(),
            likeCount: 0, commentCount: 0, isPinned: false,
            source: MOCK_USERS.find(u => u.id === currentUserId)?.isBigV ? 'pull' : 'push',
        };
        MOCK_POSTS.unshift(newPost);
        return newPost;
    }
    return request('POST', '/api/feed/post', { content });
}

// ===================== 关系接口 =====================

export async function followUser(targetUserId) {
    if (USE_MOCK) return { ok: true };
    return request('POST', '/api/follow', { targetUserId });
}

export async function unfollowUser(targetUserId) {
    if (USE_MOCK) return { ok: true };
    return request('POST', '/api/follow', { targetUserId, action: 'unfollow' });
}

export async function blockUser(targetUserId) {
    if (USE_MOCK) {
        MOCK_BLOCKED_IDS.add(targetUserId);
        return { ok: true };
    }
    return request('POST', '/api/block', { targetUserId });
}

export async function unblockUser(targetUserId) {
    if (USE_MOCK) {
        MOCK_BLOCKED_IDS.delete(targetUserId);
        return { ok: true };
    }
    return request('POST', '/api/block', { targetUserId, action: 'unblock' });
}

export async function getBlockedUserIds() {
    if (USE_MOCK) {
        return [...MOCK_BLOCKED_IDS];
    }
    const res = await request('GET', '/api/block');
    return res?.blockedIds || [];
}

export async function getFollowingIds() {
    if (USE_MOCK) {
        return [];
    }
    const res = await request('GET', '/api/follow/ids');
    return res?.ids || [];
}

export async function getFollowingList() {
    if (USE_MOCK) {
        return MOCK_USERS.filter(u => u.id !== currentUserId).slice(0, 5);
    }
    return request('GET', '/api/follow/list');
}

// ===================== 置顶接口 =====================

export async function pinPost(postId, pinned = true) {
    if (USE_MOCK) {
        const p = MOCK_POSTS.find(p => p.id === postId);
        if (p) p.isPinned = pinned;
        return { ok: true };
    }
    return request('POST', '/api/feed/pin', { postId, pinned });
}

// ===================== 互动接口 =====================

export async function likePost(postId) {
    if (USE_MOCK) {
        const p = MOCK_POSTS.find(p => p.id === postId);
        if (p) p.likeCount++;
        return { ok: true };
    }
    return request('POST', '/api/feed/like', { postId });
}

export async function getComments(postId) {
    if (USE_MOCK) {
        return MOCK_COMMENTS.filter(c => c.postId === postId);
    }
    return request('GET', `/api/feed/comments?postId=${postId}`);
}

// ===================== 调试接口（可视化面板用）=====================

export async function getFanoutStats() {
    if (USE_MOCK) {
        const u = MOCK_USERS.find(u => u.id === currentUserId);
        return {
            lastPostFanoutCount: u?.isBigV ? 1 : (u?.followerCount || 0),
            mode: u?.isBigV ? 'pull' : 'push',
            isBigV: u?.isBigV || false,
        };
    }
    return request('GET', '/api/debug/fanout-stats');
}

export async function getFeedSource() {
    if (USE_MOCK) {
        const push = MOCK_POSTS.filter(p => p.source === "push").length; const pull = MOCK_POSTS.filter(p => p.source === "pull").length; return { pushCount: push, pullCount: pull, total: push + pull, pushRatio: push/(push+pull), pullRatio: pull/(push+pull), catchupCount: 0 };
    }
    return request('GET', '/api/debug/feed-source');
}

export async function getDbRoute() {
    if (USE_MOCK) {
        return { lastQuery: 'slave', lastWrite: 'master', replicationDelay: 12 };
    }
    return request('GET', '/api/debug/db-route');
}
