import { chromium } from 'playwright-core'
import fs from 'node:fs/promises'
import path from 'node:path'

const API_BASE = process.env.API_BASE || 'http://127.0.0.1:8080'
const ADMIN_BASE = process.env.ADMIN_BASE || 'http://127.0.0.1:5173'
const USER_BASE = process.env.USER_BASE || 'http://127.0.0.1:5174'
const CHROME_PATH = process.env.CHROME_PATH || 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'
const SHOT_DIR = process.env.SCREENSHOT_DIR || path.resolve('docs/screenshots')

const pageErrors = []
const shots = []

async function api(pathname, { method = 'GET', token, body } = {}) {
  const response = await fetch(`${API_BASE}${pathname}`, {
    method,
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(body ? { 'Content-Type': 'application/json; charset=utf-8' } : {})
    },
    body: body ? JSON.stringify(body) : undefined
  })
  const payload = await response.json()
  if (!response.ok || payload.code !== 1) {
    throw new Error(`${method} ${pathname} failed: HTTP ${response.status} ${payload.message || ''}`)
  }
  return payload.data
}

async function seedOrders() {
  const admin = await api('/admin/login', {
    method: 'POST',
    body: { username: 'admin', password: 'admin123' }
  })
  const user = await api('/user/login', {
    method: 'POST',
    body: { username: '13800000000', password: 'admin123' }
  })
  const adminToken = admin.token
  const userToken = user.token

  await api('/user/cart', { method: 'DELETE', token: userToken })

  async function create(dishId, quantity, remark) {
    await api('/user/cart', {
      method: 'POST',
      token: userToken,
      body: { dishId, quantity }
    })
    return api('/user/orders', {
      method: 'POST',
      token: userToken,
      body: { addressId: 1, remark }
    })
  }

  const completed = await create(1, 1, '不要餐具')
  await api(`/user/orders/${completed.id}/pay`, { method: 'PUT', token: userToken })
  await api(`/admin/orders/${completed.id}/accept`, { method: 'PUT', token: adminToken })
  await api(`/user/orders/${completed.id}/complete`, { method: 'PUT', token: userToken })

  const pendingAccept = await create(2, 2, '少油')
  await api(`/user/orders/${pendingAccept.id}/pay`, { method: 'PUT', token: userToken })

  const unpaid = await create(3, 1, '多加一份时蔬')
  await api(`/user/orders/${unpaid.id}/cancel`, {
    method: 'PUT',
    token: userToken,
    body: { reason: '临时改变计划' }
  })

  const cancelledByAdmin = await create(5, 1, '')
  await api(`/user/orders/${cancelledByAdmin.id}/pay`, { method: 'PUT', token: userToken })
  await api(`/admin/orders/${cancelledByAdmin.id}/accept`, { method: 'PUT', token: adminToken })
  await api(`/admin/orders/${cancelledByAdmin.id}/cancel`, {
    method: 'PUT',
    token: adminToken,
    body: { reason: '食材售罄，暂时无法制作' }
  })

  await create(6, 1, '冰一点')
  await api('/user/cart', { method: 'DELETE', token: userToken })

  return { adminToken, userToken }
}

async function screenshot(page, name, options = {}) {
  const file = path.join(SHOT_DIR, name)
  await page.screenshot({ path: file, animations: 'disabled', ...options })
  shots.push(file)
  console.log(`screenshot: ${file}`)
}

async function newPage(browser, viewport) {
  const context = await browser.newContext({
    viewport,
    deviceScaleFactor: 1,
    locale: 'zh-CN',
    timezoneId: 'Asia/Shanghai'
  })
  const page = await context.newPage()
  page.on('pageerror', (error) => pageErrors.push(`pageerror: ${error.message}`))
  page.on('console', (message) => {
    if (message.type() === 'error') pageErrors.push(`console: ${message.text()} @ ${message.location().url || 'unknown'}`)
  })
  return page
}

async function waitForIdle(page) {
  await page.waitForLoadState('networkidle').catch(() => {})
  await page.evaluate(() => document.fonts.ready)
  await page
    .waitForFunction(() => !document.querySelector('.el-message') && !document.querySelector('.van-toast'), { timeout: 6000 })
    .catch(() => {})
  await page.waitForTimeout(500)
}

async function adminFlow(browser) {
  const page = await newPage(browser, { width: 1440, height: 1000 })
  await page.goto(`${ADMIN_BASE}/#/login`, { waitUntil: 'networkidle' })
  await waitForIdle(page)
  await screenshot(page, 'admin-login.png')

  await page.locator('input[placeholder="请输入管理员账号"]').fill('admin')
  await page.locator('input[placeholder="请输入登录密码"]').fill('admin123')
  await page.getByRole('button', { name: /进入管理台/ }).click()
  await page.waitForURL(/#\/orders/, { timeout: 15000 })
  await page.waitForSelector('.order-stats')
  await page.waitForFunction(() => document.querySelectorAll('.el-table__body tbody tr').length >= 4)
  await waitForIdle(page)
  await screenshot(page, 'admin-orders.png')

  await page.getByRole('button', { name: '详情' }).first().click()
  await page.getByText('ORDER DETAIL').waitFor({ timeout: 10000 })
  await page.waitForTimeout(400)
  await screenshot(page, 'admin-order-detail.png', { fullPage: false })
  await page.keyboard.press('Escape')

  const orderTable = page.locator('.table-card')
  await orderTable.getByRole('button', { name: '接单', exact: true }).first().click()
  const acceptBox = page.locator('.el-message-box')
  await acceptBox.waitFor({ timeout: 8000 })
  await acceptBox.locator('.el-message-box__btns .el-button--primary').click()
  await waitForIdle(page)

  await orderTable.getByRole('button', { name: '取消', exact: true }).first().click()
  const cancelBox = page.locator('.el-message-box')
  await cancelBox.waitFor({ timeout: 8000 })
  await cancelBox.locator('input').fill('UI 联调取消订单')
  await cancelBox.locator('.el-message-box__btns .el-button--primary').click()
  await waitForIdle(page)

  await page.goto(`${ADMIN_BASE}/#/dishes`, { waitUntil: 'networkidle' })
  await page.waitForSelector('.dish-cell')
  await waitForIdle(page)
  await screenshot(page, 'admin-dishes.png')

  await page.goto(`${ADMIN_BASE}/#/categories`, { waitUntil: 'networkidle' })
  await page.waitForSelector('.category-name')
  await waitForIdle(page)
  await screenshot(page, 'admin-categories.png')

  await page.getByRole('button', { name: '新增分类' }).click()
  const categoryDialog = page.locator('.el-dialog')
  await categoryDialog.waitFor()
  await categoryDialog.locator('input[placeholder="例如：轻食沙拉"]').fill('UI测试分类')
  await categoryDialog.locator('textarea[placeholder="一句话介绍这个分类"]').fill('phase4 integration')
  await page.getByRole('button', { name: '保存分类' }).click()
  await waitForIdle(page)
  await page.getByText('UI测试分类', { exact: true }).waitFor()

  let categoryRow = page.locator('.el-table__row').filter({ hasText: 'UI测试分类' })
  await categoryRow.getByRole('button', { name: '编辑' }).click()
  await categoryDialog.waitFor()
  await categoryDialog.locator('input[placeholder="例如：轻食沙拉"]').fill('UI测试分类改')
  await page.getByRole('button', { name: '保存分类' }).click()
  await waitForIdle(page)
  await page.getByText('UI测试分类改', { exact: true }).waitFor()

  categoryRow = page.locator('.el-table__row').filter({ hasText: 'UI测试分类改' })
  await categoryRow.getByRole('button', { name: '删除' }).click()
  const categoryConfirm = page.locator('.el-message-box')
  await categoryConfirm.waitFor()
  await categoryConfirm.locator('.el-message-box__btns .el-button--primary').click()
  await waitForIdle(page)
  await page.waitForFunction(() => ![...document.querySelectorAll('.el-table__row')].some((el) => el.textContent.includes('UI测试分类改')))

  await page.goto(`${ADMIN_BASE}/#/dishes`, { waitUntil: 'networkidle' })
  await page.waitForSelector('.dish-cell')
  await waitForIdle(page)
  await page.getByRole('button', { name: '新增菜品' }).click()
  const dishDialog = page.locator('.el-dialog')
  await dishDialog.waitFor()
  await dishDialog.locator('input[placeholder="例如：鸡胸肉藜麦沙拉"]').fill('UI测试菜品')
  const priceInput = dishDialog.locator('.el-input-number input').first()
  await priceInput.fill('26.50')
  await priceInput.press('Tab')
  await page.getByRole('button', { name: '保存菜品' }).click()
  await waitForIdle(page)
  await page.getByText('UI测试菜品', { exact: true }).waitFor()

  let dishRow = page.locator('.el-table__row').filter({ hasText: 'UI测试菜品' })
  await dishRow.getByRole('button', { name: '编辑' }).click()
  await dishDialog.waitFor()
  await dishDialog.locator('input[placeholder="例如：鸡胸肉藜麦沙拉"]').fill('UI测试菜品改')
  await page.getByRole('button', { name: '保存菜品' }).click()
  await waitForIdle(page)
  await page.getByText('UI测试菜品改', { exact: true }).waitFor()

  dishRow = page.locator('.el-table__row').filter({ hasText: 'UI测试菜品改' })
  await dishRow.locator('.el-switch').click()
  await waitForIdle(page)
  await page.locator('.el-table__row').filter({ hasText: 'UI测试菜品改' }).getByText('下架').waitFor()
  await page.locator('.el-table__row').filter({ hasText: 'UI测试菜品改' }).getByRole('button', { name: '删除' }).click()
  const dishConfirm = page.locator('.el-message-box')
  await dishConfirm.waitFor()
  await dishConfirm.locator('.el-message-box__btns .el-button--primary').click()
  await waitForIdle(page)
  await page.waitForFunction(() => ![...document.querySelectorAll('.el-table__row')].some((el) => el.textContent.includes('UI测试菜品改')))

  await page.context().close()
}

async function userFlow(browser) {
  const page = await newPage(browser, { width: 430, height: 932 })
  await page.goto(`${USER_BASE}/#/login`, { waitUntil: 'networkidle' })
  await waitForIdle(page)
  await screenshot(page, 'user-login.png')

  await page.locator('input[placeholder="请输入手机号或账号"]').fill('13800000000')
  await page.locator('input[placeholder="请输入密码"]').fill('admin123')
  await page.getByRole('button', { name: '开始点餐' }).click()
  await page.waitForURL(/#\/menu/, { timeout: 15000 })
  await page.waitForSelector('.dish-card')
  await waitForIdle(page)
  const tabbarItems = await page.locator('.van-tabbar-item').count()
  const tabbarBox = await page.locator('.van-tabbar').boundingBox()
  if (tabbarItems !== 4 || !tabbarBox || tabbarBox.x < 0) {
    throw new Error(`User tabbar layout invalid: items=${tabbarItems}, box=${JSON.stringify(tabbarBox)}`)
  }
  await screenshot(page, 'user-menu.png', { fullPage: true })

  await page.locator('.dish-card').first().click()
  await page.waitForURL(/#\/dishes\/\d+/, { timeout: 15000 })
  await page.locator('.dish-content h1').waitFor()
  await waitForIdle(page)
  await screenshot(page, 'user-dish-detail.png', { fullPage: true })
  await page.goBack()
  await page.waitForSelector('.dish-card')
  await waitForIdle(page)

  await page.locator('button[aria-label^="加入购物车"]').first().click()
  await waitForIdle(page)

  await page.goto(`${USER_BASE}/#/cart`, { waitUntil: 'networkidle' })
  await page.getByText(/已选 \d+ 件/).waitFor()
  await waitForIdle(page)
  await screenshot(page, 'user-cart.png', { fullPage: true })

  await page.getByRole('button', { name: '去结算' }).click()
  await page.waitForURL(/#\/checkout/, { timeout: 15000 })
  await page.getByText('收货地址').waitFor()
  await waitForIdle(page)
  await screenshot(page, 'user-checkout.png', { fullPage: true })

  await page.getByRole('button', { name: '提交订单' }).click()
  await page.waitForURL(/#\/orders\/\d+/, { timeout: 15000 })
  await page.getByRole('heading', { name: '待付款' }).waitFor()
  await waitForIdle(page)
  await screenshot(page, 'user-order-created.png', { fullPage: true })

  await page.getByRole('button', { name: '模拟支付' }).click()
  await page.getByRole('button', { name: '确认支付' }).click()
  await page.getByRole('heading', { name: '待接单' }).waitFor({ timeout: 15000 })
  await waitForIdle(page)
  await screenshot(page, 'user-order-paid.png', { fullPage: true })

  await page.goto(`${USER_BASE}/#/orders`, { waitUntil: 'networkidle' })
  await page.waitForSelector('.order-card')
  await waitForIdle(page)
  await screenshot(page, 'user-orders.png', { fullPage: true })

  await page.goto(`${USER_BASE}/#/address`, { waitUntil: 'networkidle' })
  await page.waitForSelector('.address-card')
  await waitForIdle(page)
  await screenshot(page, 'user-address.png', { fullPage: true })

  await page.locator('.van-nav-bar__text').click()
  const addressPopup = page.locator('.address-popup')
  await addressPopup.waitFor()
  await addressPopup.locator('input[placeholder="请输入姓名"]').fill('UI测试收货人')
  await addressPopup.locator('input[placeholder="请输入手机号"]').fill('13900000000')
  const regionInputs = addressPopup.locator('input[placeholder="例如：上海市"]')
  await regionInputs.nth(0).fill('上海市')
  await regionInputs.nth(1).fill('上海市')
  await addressPopup.locator('input[placeholder="例如：徐汇区"]').fill('浦东新区')
  await addressPopup.locator('textarea[placeholder="街道、门牌号、楼层房间号"]').fill('UI测试路 8 号')
  await addressPopup.locator('input[placeholder="家 / 公司 / 学校"]').fill('测试')
  await page.getByRole('button', { name: '保存地址' }).click()
  await waitForIdle(page)
  await page.getByText('UI测试收货人', { exact: true }).waitFor()

  let addressCard = page.locator('.address-card').filter({ hasText: 'UI测试收货人' })
  await addressCard.getByRole('button', { name: '设为默认' }).click()
  await waitForIdle(page)
  addressCard = page.locator('.address-card').filter({ hasText: 'UI测试收货人' })
  await addressCard.getByText('默认', { exact: true }).waitFor()

  await addressCard.getByRole('button', { name: '删除' }).click()
  const addressDialog = page.locator('.van-dialog')
  await addressDialog.waitFor()
  await addressDialog.getByRole('button', { name: '删除' }).click()
  await waitForIdle(page)
  await page.waitForFunction(() => ![...document.querySelectorAll('.address-card')].some((el) => el.textContent.includes('UI测试收货人')))

  await page.context().close()
}

async function main() {
  await fs.mkdir(SHOT_DIR, { recursive: true })
  await seedOrders()
  console.log('seed data ready')

  const browser = await chromium.launch({
    headless: true,
    executablePath: CHROME_PATH,
    args: ['--no-sandbox', '--disable-gpu']
  })

  try {
    await adminFlow(browser)
    await userFlow(browser)
  } finally {
    await browser.close()
  }

  if (pageErrors.length) {
    console.error('Browser errors:')
    for (const error of pageErrors) console.error(`- ${error}`)
    process.exitCode = 1
  } else {
    console.log(`UI_CHECK_OK screenshots=${shots.length}`)
  }
}

main().catch((error) => {
  console.error(error)
  process.exit(1)
})
