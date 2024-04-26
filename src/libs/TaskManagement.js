class TaskManagement {
    constructor(previousTask = []) {
        this.tasks = previousTask
    }

    getDtoAllTasks(newTasks) {
        newTasks.forEach((newTask) =>
            this.getDtoAllTask(newTask.taskId, newTask.title , newTask.assignees, newTask.status)
        )
    }

    getDtoAllTask(taskId, title , assignees , status) {
        this.tasks.push({
            taskId: taskId,
            title: title,
            assignees: assignees,
            status: status
        })
    }

    getTasks() {
        return this.tasks
    }
}

export { TaskManagement }