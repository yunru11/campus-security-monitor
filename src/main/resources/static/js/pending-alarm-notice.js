(() => {
    const NOTICE_ID = 'pending-alarm-notice'

    function formatTime(value) {
        if (!value) return '时间未知'

        const date = new Date(value)
        if (Number.isNaN(date.getTime())) return String(value)

        return new Intl.DateTimeFormat('zh-CN', {
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        }).format(date)
    }

    function appendText(parent, className, text) {
        const element = document.createElement('span')
        element.className = className
        element.textContent = text
        parent.appendChild(element)
        return element
    }

    function showPendingAlarmNotice(alarms) {
        const existingNotice = document.getElementById(NOTICE_ID)
        existingNotice?.remove()

        const pendingAlarms = alarms
            .filter(item => item && item.status === '待处置')
            .sort((a, b) => {
                const timeDifference = new Date(b.eventTime || 0) - new Date(a.eventTime || 0)
                return timeDifference || Number(b.id || 0) - Number(a.id || 0)
            })

        if (pendingAlarms.length === 0) return

        const notice = document.createElement('aside')
        notice.id = NOTICE_ID
        notice.className = 'pending-alarm-notice'
        notice.setAttribute('role', 'alert')
        notice.setAttribute('aria-live', 'assertive')

        const header = document.createElement('div')
        header.className = 'pending-alarm-notice-header'

        const heading = document.createElement('div')
        heading.className = 'pending-alarm-notice-heading'
        const icon = document.createElement('span')
        icon.className = 'pending-alarm-notice-icon'
        icon.setAttribute('aria-hidden', 'true')
        icon.innerHTML = '<svg viewBox="0 0 24 24"><path d="M12 9v4M12 17h.01M10.3 3.8 2.4 17.5A2 2 0 0 0 4.1 20h15.8a2 2 0 0 0 1.7-2.5L13.7 3.8a2 2 0 0 0-3.4 0Z"/></svg>'
        heading.appendChild(icon)

        const titleGroup = document.createElement('div')
        appendText(titleGroup, 'pending-alarm-notice-label', '待处置提醒')
        const title = document.createElement('strong')
        title.textContent = `当前有 ${pendingAlarms.length} 条告警等待处置`
        titleGroup.appendChild(title)
        heading.appendChild(titleGroup)
        header.appendChild(heading)

        const closeButton = document.createElement('button')
        closeButton.className = 'pending-alarm-notice-close'
        closeButton.type = 'button'
        closeButton.setAttribute('aria-label', '关闭待处置告警提示')
        closeButton.textContent = '×'
        closeButton.addEventListener('click', () => notice.remove())
        header.appendChild(closeButton)
        notice.appendChild(header)

        const list = document.createElement('div')
        list.className = 'pending-alarm-notice-list'
        pendingAlarms.slice(0, 3).forEach(item => {
            const row = document.createElement('div')
            row.className = 'pending-alarm-notice-item'

            const main = document.createElement('div')
            appendText(main, 'pending-alarm-notice-type', item.type || '未命名告警')
            appendText(main, 'pending-alarm-notice-area', item.area || '未知区域')
            row.appendChild(main)
            appendText(row, 'pending-alarm-notice-time', formatTime(item.eventTime))
            list.appendChild(row)
        })
        notice.appendChild(list)

        const footer = document.createElement('div')
        footer.className = 'pending-alarm-notice-footer'
        appendText(footer, 'pending-alarm-notice-tip', pendingAlarms.length > 3 ? `另有 ${pendingAlarms.length - 3} 条待处理` : '请及时确认并处置')
        const link = document.createElement('a')
        link.href = 'alarm.html'
        link.textContent = '查看告警中心'
        footer.appendChild(link)
        notice.appendChild(footer)

        document.body.appendChild(notice)
    }

    async function loadPendingAlarms() {
        try {
            const response = await fetch('/alarm/list', {
                headers: { Accept: 'application/json' }
            })
            if (!response.ok) return

            const alarms = await response.json()
            if (Array.isArray(alarms)) showPendingAlarmNotice(alarms)
        } catch (_) {
            // 页面主体已有独立的数据错误提示，提醒加载失败时不阻断页面使用。
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', loadPendingAlarms, { once: true })
    } else {
        loadPendingAlarms()
    }
})()
